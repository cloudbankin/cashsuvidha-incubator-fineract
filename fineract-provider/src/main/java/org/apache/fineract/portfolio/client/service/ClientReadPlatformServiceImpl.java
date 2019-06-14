/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
import org.apache.fineract.portfolio.client.data.ClientTimelineData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.joda.time.Days;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.client.data.LoanDetailData;



@Service
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    // data mappers
    private final PaginationHelper<ClientData> paginationHelper = new PaginationHelper<>();
    private final ClientMapper clientMapper = new ClientMapper();
    private final ClientLookupMapper lookupMapper = new ClientLookupMapper();
    private final ClientMembersOfGroupMapper membersOfGroupMapper = new ClientMembersOfGroupMapper();
    private final ParentGroupsMapper clientGroupsMapper = new ParentGroupsMapper();
    
    private final AddressReadPlatformService addressReadPlatformService;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final ColumnValidator columnValidator;
    
    
    
    

    @Autowired
    public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final SavingsProductReadPlatformService savingsProductReadPlatformService,
            final AddressReadPlatformService addressReadPlatformService,
            final ConfigurationReadPlatformService configurationReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService,
            final ColumnValidator columnValidator) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;
        this.addressReadPlatformService=addressReadPlatformService;
        this.configurationReadPlatformService=configurationReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.columnValidator = columnValidator;
    }

    @Override
    public ClientData retrieveTemplate(final Long officeId, final boolean staffInSelectedOfficeOnly) {
        this.context.authenticatedUser();

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);
        AddressData address=null;

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookupByType(null);
        
        final GlobalConfigurationPropertyData configuration=this.configurationReadPlatformService.retrieveGlobalConfiguration("Enable-Address");
        
        final Boolean isAddressEnabled=configuration.isEnabled(); 
        if(isAddressEnabled)
        {
        	 address = this.addressReadPlatformService.retrieveTemplate();
        }
        
       

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }
        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));
        
        final List<CodeValueData> clientNonPersonConstitutionOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION));
        
        final List<CodeValueData> clientNonPersonMainBusinessLineOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE));
        
        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.CLIENT.getName(), null);

        return ClientData.template(defaultOfficeId, new LocalDate(), offices, staffOptions, null, genderOptions, savingsProductDatas,
                clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions, clientNonPersonMainBusinessLineOptions,
                clientLegalFormOptions,address,isAddressEnabled, datatableTemplates);
    }

    @Override
   // @Transactional(readOnly=true)
    public Page<ClientData> retrieveAll(final SearchParameters searchParameters) {

        final String userOfficeHierarchy = this.context.officeHierarchy();
        final String underHierarchySearchString = userOfficeHierarchy + "%";
        final String appUserID = String.valueOf(context.authenticatedUser().getId());

        // if (searchParameters.isScopedByOfficeHierarchy()) {
        // this.context.validateAccessRights(searchParameters.getHierarchy());
        // underHierarchySearchString = searchParameters.getHierarchy() + "%";
        // }
        List<Object> paramList = new ArrayList<>(Arrays.asList(underHierarchySearchString, underHierarchySearchString));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.clientMapper.schema());
        sqlBuilder.append(" where (o.hierarchy like ? or transferToOffice.hierarchy like ?) ");
        
        if(searchParameters.isSelfUser()){
        	sqlBuilder.append(" and c.id in (select umap.client_id from m_selfservice_user_client_mapping as umap where umap.appuser_id = ? ) ");
        	paramList.add(appUserID);
        }

        final String extraCriteria = buildSqlStringFromClientCriteria(this.clientMapper.schema(), searchParameters, paramList);
        
        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), paramList.toArray(), this.clientMapper);
    }

    private String buildSqlStringFromClientCriteria(String schemaSql, final SearchParameters searchParameters, List<Object> paramList) {

        String sqlSearch = searchParameters.getSqlSearch();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String displayName = searchParameters.getName();
        final String firstname = searchParameters.getFirstname();
        final String lastname = searchParameters.getLastname();

        String extraCriteria = "";
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " c.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "c.display_name ");
            extraCriteria = " and (" + sqlSearch + ")";
            this.columnValidator.validateSqlInjection(schemaSql, sqlSearch);
        }

        if (officeId != null) {
            extraCriteria += " and c.office_id = ? ";
            paramList.add(officeId);
        }

        if (externalId != null) {
        	paramList.add(ApiParameterHelper.sqlEncodeString(externalId));
            extraCriteria += " and c.external_id like ? " ;
        }

        if (displayName != null) {
            //extraCriteria += " and concat(ifnull(c.firstname, ''), if(c.firstname > '',' ', '') , ifnull(c.lastname, '')) like "
        	paramList.add("%" + displayName + "%");
        	extraCriteria += " and c.display_name like ? ";
        }

        if (firstname != null) {
        	paramList.add(ApiParameterHelper.sqlEncodeString(firstname));
            extraCriteria += " and c.firstname like ? " ;
        }

        if (lastname != null) {
        	paramList.add(ApiParameterHelper.sqlEncodeString(lastname));
            extraCriteria += " and c.lastname like ? ";
        }

        if (searchParameters.isScopedByOfficeHierarchy()) {
        	paramList.add(ApiParameterHelper.sqlEncodeString(searchParameters.getHierarchy() + "%"));
            extraCriteria += " and o.hierarchy like ? ";
        }
        
        if(searchParameters.isOrphansOnly()){
        	extraCriteria += " and c.id NOT IN (select client_id from m_group_client) ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    
    
    
    
    
    
    
    // collection Efficiency report changes
    @Override
    public Workbook retrieveCollectionEfficiencyreport(String fromDatee, String toDatee, Long office) {
    	 //SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	 SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss zzz ", Locale.ENGLISH);
//SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	 
    	 Date fromdate = null;
    	 Date todate = null;
    	 try {
			 Date tempDate = sdf3.parse(fromDatee);
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			 String date1= format1.format(tempDate);
			 fromdate = format1.parse(date1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 try {
			 Date tempDate = sdf3.parse(toDatee);
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			 String date1= format1.format(tempDate);
			 todate = format1.parse(date1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	 
    	 
    	 Long officeid = office;
    	 Workbook workbook = new HSSFWorkbook();
         CreationHelper creationHelper = workbook.getCreationHelper();
         CellStyle style = workbook.createCellStyle(); //Create new style
         style.setWrapText(true); //Set wordwrap
     	CellStyle style1 = workbook.createCellStyle();
 		style1.setDataFormat(creationHelper.createDataFormat().getFormat(
 				"ddMMyyyy"));

       	Sheet accountSheet = workbook.createSheet("Account");
       	
       	//Column test = accountSheet.setDefaultColumnStyle(0, style);
       	
       	String a[]={"Particulars /Months","Opening Arrears (A)","Current Demand (B)","Total Demand (A+B)","Arrears Collection (C)","Prepayment/Advance Collection (D)",
       			"Current Month Collection (E)","Total Collection (C+D+E)","Closing Arrears [(A+B)-C-E]","Arrears Collection (C/A)","Current Collection ((D/B)","Collection Efficiency (C+D+E) / (A+B)"};

        for(int kk=0;kk<a.length;kk++)
    	{
    		Row headerLoan =  accountSheet.createRow(kk+1);
        	headerLoan.setRowStyle(style);
        	headerLoan.createCell(0).setCellValue(a[kk]);
    	}
        
       
       
       accountSheet.addMergedRegion(new CellRangeAddress(0,0,2,7));
    	Row rowSecond = accountSheet.createRow(0);
     //rowSecond.setHeight((short) 1000);
//     	CellStyle cs = workbook.createCellStyle();
//         cs.setWrapText(true);
        Cell address = rowSecond.createCell(2);
 //        address.setCellStyle(cs);
        address.setCellValue("COLLECTION EFFICIENCY REPORT");
      
    	 
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        for(int i=0;i<12;i++)
        {
        	if (fromdate.compareTo(todate) < 0)
        	{
        		//Date date= new Date(sdf.parse(fromdate).getTime());
        		Calendar cal = Calendar.getInstance();
        		 cal.setTime(fromdate);
        		 //Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(fromdate.toString()));
        	      //Add 4months and 5days
        		 
        		 
        		 int year = cal.get(Calendar.YEAR);
        		 int mon = cal.get(Calendar.MONTH)+1;
        		 String y = String.valueOf(year);
        		 String m = String.valueOf(mon);
        		 String mm = "Jan";
        		 switch(mon)
        		 {
        		 case 1:
        			 mm = "Jan" ;
        			 break;
        		 case 2:
        			 mm = "Feb" ;
        			 break;
        		 case 3:
        			 mm = "Mar" ;
        			 break;
        		 case 4:
        			 mm = "Apr" ;
        			 break;
        		 case 5:
        			 mm = "May" ;
        			 break;
        		 case 6:
        			 mm = "Jun" ;
        			 break;
        		 case 7:
        			 mm = "Jul" ;
        			 break;
        		 case 8:
        			 mm = "Aug" ;
        			 break;
        		 case 9:
        			 mm = "Sep" ;
        			 break;
        		 case 10:
        			 mm = "Oct" ;
        			 break;
        		 case 11:
        			 mm = "Nov" ;
        			 break;
        		 case 12:
        			 mm = "Dec" ;
        			 break;
        		 }
        		 String fullName  = mm + "_"+y;
        		 
        final LoansDetailsMapper lm = new LoansDetailsMapper();          
        final List <LoanDetailData>  loanDetails = this.jdbcTemplate.query(lm.schema(),lm,  new Object[] {fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,fromdate,officeid});
        cal.add(Calendar.MONTH, 1);
       // Date tempDate = sdf3.parse(cal.getTime());
		 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		 String date1= format1.format(cal.getTime());
		 try {
			fromdate = format1.parse(date1);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        

    	
        for(int k=0;k<loanDetails.size();k++)
        {
        	LoanDetailData tempLoan = loanDetails.get(k);
        	for(int j=0;j<12;j++)
        	{
        	
        	Row loanRow = accountSheet.getRow(j+1);
        	try {
            	//loanRow.setRowStyle(style);
        		if(j==0)
        		{
        			loanRow.createCell(i+1).setCellValue(fullName);
        		}
            	if(j==1)
            	{
            	loanRow.createCell(i+1).setCellValue(tempLoan.getOpeningarrears());
            	}
            	if(j==2)
            	{
            	loanRow.createCell(i+1).setCellValue(tempLoan.getCurrentdemand());
            	}
            	long totaldemand = tempLoan.getOpeningarrears()+tempLoan.getCurrentdemand();
            	if(j==3)
            	{
            	loanRow.createCell(i+1).setCellValue(totaldemand); 
            	}
            	if(j==4)
            	{
            	loanRow.createCell(i+1).setCellValue(tempLoan.getArrearcollection());
            	}
            	if(j==5)
            	{
            	loanRow.createCell(i+1).setCellValue(tempLoan.getAdvancecollection());
            	}
            	
            	if(j==6)
            	{
            	loanRow.createCell(i+1).setCellValue(tempLoan.getCurrentcollection());
            	}
            	
            	long totalcollection = tempLoan.getArrearcollection()+tempLoan.getAdvancecollection()+tempLoan.getCurrentcollection();
            	if(j==7)
            	{
            	loanRow.createCell(i+1).setCellValue(totalcollection);
            	}
            	
            	long closingarrear = (tempLoan.getOpeningarrears()+tempLoan.getCurrentdemand())-tempLoan.getArrearcollection()-tempLoan.getCurrentcollection();
            	if(j==8)
            	{
            	loanRow.createCell(i+1).setCellValue(closingarrear);
            	}
            	long parrearcollection = (tempLoan.getArrearcollection()*100)/tempLoan.getOpeningarrears();
            	if(j==9)
            	{
            	loanRow.createCell(i+1).setCellValue(parrearcollection);
            	}
            	long pcurrentcollection = (tempLoan.getCurrentcollection()*100)/tempLoan.getCurrentdemand();
            	if(j==10)
            	{
            	loanRow.createCell(i+1).setCellValue(pcurrentcollection);
            	}
            	long pcollectionefficiency = ((tempLoan.getArrearcollection()+tempLoan.getCurrentcollection()+tempLoan.getAdvancecollection())*100)/(tempLoan.getOpeningarrears()+tempLoan.getCurrentdemand());
            	if(j==11)
            	{
            	loanRow.createCell(i+1).setCellValue(pcollectionefficiency);
            	}
            	
            	
    			
    			
            				
            }
           
        	
        catch(Exception e) {
       	loanRow.createCell(16).setCellValue("Error Occured" + e.getMessage());
        }
        }
       	
        }
        }
        }
        
       
        accountSheet.autoSizeColumn(0);
        accountSheet.autoSizeColumn(1);
        accountSheet.autoSizeColumn(2);
        accountSheet.autoSizeColumn(3);
        accountSheet.autoSizeColumn(4);
        accountSheet.autoSizeColumn(5);
        accountSheet.autoSizeColumn(6);
        
    	return workbook;
    }
   
    
    
    
    
    
    
    @Override
    public ClientData retrieveOne(final Long clientId) {
        try {
            final String hierarchy = this.context.officeHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.clientMapper.schema()
                    + " where ( o.hierarchy like ? or transferToOffice.hierarchy like ?) and c.id = ?";
            final ClientData clientData = this.jdbcTemplate.queryForObject(sql, this.clientMapper, new Object[] { hierarchySearchString,
                    hierarchySearchString, clientId });

            final String clientGroupsSql = "select " + this.clientGroupsMapper.parentGroupsSchema();

            final Collection<GroupGeneralData> parentGroups = this.jdbcTemplate.query(clientGroupsSql, this.clientGroupsMapper,
                    new Object[] { clientId });

            return ClientData.setParentGroups(clientData, parentGroups);

        } catch (final EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientData> retrieveAllForLookup(final String extraCriteria) {

        String sql = "select " + this.lookupMapper.schema();

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
            this.columnValidator.validateSqlInjection(sql, extraCriteria);
        }        
        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] {});
    }

    @Override
    public Collection<ClientData> retrieveAllForLookupByOfficeId(final Long officeId) {

        final String sql = "select " + this.lookupMapper.schema() + " where c.office_id = ? and c.status_enum != ?";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { officeId, ClientStatus.CLOSED.getValue() });
    }

    @Override
    public Collection<ClientData> retrieveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema() + " where o.hierarchy like ? and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, new Object[] { hierarchySearchString, groupId });
    }

    @Override
    public Collection<ClientData> retrieveActiveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema()
                + " where o.hierarchy like ? and pgc.group_id = ? and c.status_enum = ? ";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper,
                new Object[] { hierarchySearchString, groupId, ClientStatus.ACTIVE.getValue() });
    }

    private static final class ClientMembersOfGroupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientMembersOfGroupMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder
                    .append("c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum,c.sub_status as subStatus, ");
            sqlBuilder
                    .append("cvSubStatus.code_value as subStatusValue,cvSubStatus.code_description as subStatusDesc,c.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("c.transfer_to_office_id as transferToOfficeId, transferToOffice.name as transferToOfficeName, ");
            sqlBuilder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            sqlBuilder.append("c.fullname as fullname, c.display_name as displayName, ");
            sqlBuilder.append("c.mobile_no as mobileNo, ");
			sqlBuilder.append("c.is_staff as isStaff, ");
            sqlBuilder.append("c.date_of_birth as dateOfBirth, ");
            sqlBuilder.append("c.gender_cv_id as genderId, ");
            sqlBuilder.append("cv.code_value as genderValue, ");
            sqlBuilder.append("c.client_type_cv_id as clienttypeId, ");
            sqlBuilder.append("cvclienttype.code_value as clienttypeValue, ");
            sqlBuilder.append("c.client_classification_cv_id as classificationId, ");
            sqlBuilder.append("cvclassification.code_value as classificationValue, ");
            sqlBuilder.append("c.legal_form_enum as legalFormEnum, ");
            sqlBuilder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            sqlBuilder.append("c.staff_id as staffId, s.display_name as staffName,");
            sqlBuilder.append("c.default_savings_product as savingsProductId, sp.name as savingsProductName, ");
            sqlBuilder.append("c.default_savings_account as savingsAccountId, ");

            sqlBuilder.append("c.submittedon_date as submittedOnDate, ");
            sqlBuilder.append("sbu.username as submittedByUsername, ");
            sqlBuilder.append("sbu.firstname as submittedByFirstname, ");
            sqlBuilder.append("sbu.lastname as submittedByLastname, ");

            sqlBuilder.append("c.closedon_date as closedOnDate, ");
            sqlBuilder.append("clu.username as closedByUsername, ");
            sqlBuilder.append("clu.firstname as closedByFirstname, ");
            sqlBuilder.append("clu.lastname as closedByLastname, ");

            sqlBuilder.append("acu.username as activatedByUsername, ");
            sqlBuilder.append("acu.firstname as activatedByFirstname, ");
            sqlBuilder.append("acu.lastname as activatedByLastname, ");
            
            sqlBuilder.append("cnp.constitution_cv_id as constitutionId, ");
            sqlBuilder.append("cvConstitution.code_value as constitutionValue, ");
            sqlBuilder.append("cnp.incorp_no as incorpNo, ");
            sqlBuilder.append("cnp.incorp_validity_till as incorpValidityTill, ");
            sqlBuilder.append("cnp.main_business_line_cv_id as mainBusinessLineId, ");
            sqlBuilder.append("cvMainBusinessLine.code_value as mainBusinessLineValue, ");
            sqlBuilder.append("cnp.remarks as remarks ");

            sqlBuilder.append("from m_client c ");
            sqlBuilder.append("join m_office o on o.id = c.office_id ");
            sqlBuilder.append("left join m_client_non_person cnp on cnp.client_id = c.id ");
            sqlBuilder.append("join m_group_client pgc on pgc.client_id = c.id ");
            sqlBuilder.append("left join m_staff s on s.id = c.staff_id ");
            sqlBuilder.append("left join m_savings_product sp on sp.id = c.default_savings_product ");
            sqlBuilder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");

            sqlBuilder.append("left join m_appuser sbu on sbu.id = c.submittedon_userid ");
            sqlBuilder.append("left join m_appuser acu on acu.id = c.activatedon_userid ");
            sqlBuilder.append("left join m_appuser clu on clu.id = c.closedon_userid ");
            sqlBuilder.append("left join m_code_value cv on cv.id = c.gender_cv_id ");
            sqlBuilder.append("left join m_code_value cvclienttype on cvclienttype.id = c.client_type_cv_id ");
            sqlBuilder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            sqlBuilder.append("left join m_code_value cvSubStatus on cvSubStatus.id = c.sub_status ");
            sqlBuilder.append("left join m_code_value cvConstitution on cvConstitution.id = cnp.constitution_cv_id ");
            sqlBuilder.append("left join m_code_value cvMainBusinessLine on cvMainBusinessLine.id = cnp.main_business_line_cv_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long subStatusId = JdbcSupport.getLong(rs, "subStatus");
            final String subStatusValue = rs.getString("subStatusValue");
            final String subStatusDesc = rs.getString("subStatusDesc");
            final boolean isActive = false;
            final CodeValueData subStatus = CodeValueData.instance(subStatusId, subStatusValue, subStatusDesc, isActive);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long transferToOfficeId = JdbcSupport.getLong(rs, "transferToOfficeId");
            final String transferToOfficeName = rs.getString("transferToOfficeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
			final boolean isStaff = rs.getBoolean("isStaff");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final Long genderId = JdbcSupport.getLong(rs, "genderId");
            final String genderValue = rs.getString("genderValue");
            final CodeValueData gender = CodeValueData.instance(genderId, genderValue);

            final Long clienttypeId = JdbcSupport.getLong(rs, "clienttypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clienttype = CodeValueData.instance(clienttypeId, clienttypeValue);

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsProductId = JdbcSupport.getLong(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");

            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");
            
            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if(legalFormEnum != null)
            		legalForm = ClientEnumerations.legalForm(legalFormEnum);
            
            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String incorpNo = rs.getString("incorpNo");
            final LocalDate incorpValidityTill = JdbcSupport.getLocalDate(rs, "incorpValidityTill");
            final Long mainBusinessLineId = JdbcSupport.getLong(rs, "mainBusinessLineId");            
            final String mainBusinessLineValue = rs.getString("mainBusinessLineValue");
            final CodeValueData mainBusinessLine = CodeValueData.instance(mainBusinessLineId, mainBusinessLineValue);
            final String remarks = rs.getString("remarks");
            
            final ClientNonPersonData clientNonPerson = new ClientNonPersonData(constitution, incorpNo, incorpValidityTill, mainBusinessLine, remarks);

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);

            return ClientData.instance(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id,
                    firstname, middlename, lastname, fullname, displayName, externalId, mobileNo, dateOfBirth, gender, activationDate,
                    imageId, staffId, staffName, timeline, savingsProductId, savingsProductName, savingsAccountId, clienttype,
                    classification, legalForm, clientNonPerson, isStaff);

        }
    }

    @Override
    public Collection<ClientData> retrieveActiveClientMembersOfCenter(final Long centerId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select "
                + this.membersOfGroupMapper.schema()
                + " left join m_group g on pgc.group_id=g.id where o.hierarchy like ? and g.parent_id = ? and c.status_enum = ? group by c.id";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper,
                new Object[] { hierarchySearchString, centerId, ClientStatus.ACTIVE.getValue() });
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientMapper() {
            final StringBuilder builder = new StringBuilder(400);

            builder.append("c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum,c.sub_status as subStatus, ");
            builder.append("cvSubStatus.code_value as subStatusValue,cvSubStatus.code_description as subStatusDesc,c.office_id as officeId, o.name as officeName, ");
            builder.append("c.transfer_to_office_id as transferToOfficeId, transferToOffice.name as transferToOfficeName, ");
            builder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            builder.append("c.fullname as fullname, c.display_name as displayName, ");
            builder.append("c.mobile_no as mobileNo, ");
			builder.append("c.is_staff as isStaff, ");
            builder.append("c.date_of_birth as dateOfBirth, ");
            builder.append("c.gender_cv_id as genderId, ");
            builder.append("cv.code_value as genderValue, ");
            builder.append("c.client_type_cv_id as clienttypeId, ");
            builder.append("cvclienttype.code_value as clienttypeValue, ");
            builder.append("c.client_classification_cv_id as classificationId, ");
            builder.append("cvclassification.code_value as classificationValue, ");
            builder.append("c.legal_form_enum as legalFormEnum, ");

            builder.append("c.submittedon_date as submittedOnDate, ");
            builder.append("sbu.username as submittedByUsername, ");
            builder.append("sbu.firstname as submittedByFirstname, ");
            builder.append("sbu.lastname as submittedByLastname, ");

            builder.append("c.closedon_date as closedOnDate, ");
            builder.append("clu.username as closedByUsername, ");
            builder.append("clu.firstname as closedByFirstname, ");
            builder.append("clu.lastname as closedByLastname, ");

            // builder.append("c.submittedon as submittedOnDate, ");
            builder.append("acu.username as activatedByUsername, ");
            builder.append("acu.firstname as activatedByFirstname, ");
            builder.append("acu.lastname as activatedByLastname, ");
            
            builder.append("cnp.constitution_cv_id as constitutionId, ");
            builder.append("cvConstitution.code_value as constitutionValue, ");
            builder.append("cnp.incorp_no as incorpNo, ");
            builder.append("cnp.incorp_validity_till as incorpValidityTill, ");
            builder.append("cnp.main_business_line_cv_id as mainBusinessLineId, ");
            builder.append("cvMainBusinessLine.code_value as mainBusinessLineValue, ");
            builder.append("cnp.remarks as remarks, ");

            builder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            builder.append("c.staff_id as staffId, s.display_name as staffName, ");
            builder.append("c.default_savings_product as savingsProductId, sp.name as savingsProductName, ");
            builder.append("c.default_savings_account as savingsAccountId ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");
            builder.append("left join m_client_non_person cnp on cnp.client_id = c.id ");
            builder.append("left join m_staff s on s.id = c.staff_id ");
            builder.append("left join m_savings_product sp on sp.id = c.default_savings_product ");
            builder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            builder.append("left join m_appuser sbu on sbu.id = c.submittedon_userid ");
            builder.append("left join m_appuser acu on acu.id = c.activatedon_userid ");
            builder.append("left join m_appuser clu on clu.id = c.closedon_userid ");
            builder.append("left join m_code_value cv on cv.id = c.gender_cv_id ");
            builder.append("left join m_code_value cvclienttype on cvclienttype.id = c.client_type_cv_id ");
            builder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            builder.append("left join m_code_value cvSubStatus on cvSubStatus.id = c.sub_status ");
            builder.append("left join m_code_value cvConstitution on cvConstitution.id = cnp.constitution_cv_id ");
            builder.append("left join m_code_value cvMainBusinessLine on cvMainBusinessLine.id = cnp.main_business_line_cv_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long subStatusId = JdbcSupport.getLong(rs, "subStatus");
            final String subStatusValue = rs.getString("subStatusValue");
            final String subStatusDesc = rs.getString("subStatusDesc");
            final boolean isActive = false;
            final CodeValueData subStatus = CodeValueData.instance(subStatusId, subStatusValue, subStatusDesc, isActive);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long transferToOfficeId = JdbcSupport.getLong(rs, "transferToOfficeId");
            final String transferToOfficeName = rs.getString("transferToOfficeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
			final boolean isStaff = rs.getBoolean("isStaff");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final Long genderId = JdbcSupport.getLong(rs, "genderId");
            final String genderValue = rs.getString("genderValue");
            final CodeValueData gender = CodeValueData.instance(genderId, genderValue);

            final Long clienttypeId = JdbcSupport.getLong(rs, "clienttypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clienttype = CodeValueData.instance(clienttypeId, clienttypeValue);

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsProductId = JdbcSupport.getLong(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");
            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");
            
            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if(legalFormEnum != null)
            		legalForm = ClientEnumerations.legalForm(legalFormEnum);
            
            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String incorpNo = rs.getString("incorpNo");
            final LocalDate incorpValidityTill = JdbcSupport.getLocalDate(rs, "incorpValidityTill");
            final Long mainBusinessLineId = JdbcSupport.getLong(rs, "mainBusinessLineId");            
            final String mainBusinessLineValue = rs.getString("mainBusinessLineValue");
            final CodeValueData mainBusinessLine = CodeValueData.instance(mainBusinessLineId, mainBusinessLineValue);
            final String remarks = rs.getString("remarks");
            
            final ClientNonPersonData clientNonPerson = new ClientNonPersonData(constitution, incorpNo, incorpValidityTill, mainBusinessLine, remarks);

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);

            return ClientData.instance(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id,
                    firstname, middlename, lastname, fullname, displayName, externalId, mobileNo, dateOfBirth, gender, activationDate,
                    imageId, staffId, staffName, timeline, savingsProductId, savingsProductName, savingsAccountId, clienttype,
                    classification, legalForm, clientNonPerson, isStaff);

        }
    }

    private static final class ParentGroupsMapper implements RowMapper<GroupGeneralData> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.account_no as accountNo, gp.display_name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ?";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final String accountNo = rs.getString("accountNo");

            return GroupGeneralData.lookup(groupId, accountNo, groupName);
        }
    }

    private static final class ClientLookupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientLookupMapper() {
            final StringBuilder builder = new StringBuilder(200);

            builder.append("c.id as id, c.display_name as displayName, ");
            builder.append("c.office_id as officeId, o.name as officeName ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.lookup(id, displayName, officeId, officeName);
        }
    }

    @Override
    public ClientData retrieveClientByIdentifier(final Long identifierTypeId, final String identifierKey) {
        try {
            final ClientIdentifierMapper mapper = new ClientIdentifierMapper();

            final String sql = "select " + mapper.clientLookupByIdentifierSchema();

            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { identifierTypeId, identifierKey });
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class ClientIdentifierMapper implements RowMapper<ClientData> {

        public String clientLookupByIdentifierSchema() {
            return "c.id as id, c.account_no as accountNo, c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName," + "c.office_id as officeId, o.name as officeName "
                    + " from m_client c, m_office o, m_client_identifier ci " + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, accountNo, firstname, middlename, lastname, fullname, displayName, officeId, officeName);
        }
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public ClientData retrieveAllNarrations(final String clientNarrations) {
        final List<CodeValueData> narrations = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode(clientNarrations));
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> clientNonPersonConstitutionOptions = null;
        final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        return ClientData.template(null, null, null, null, narrations, null, null, clientTypeOptions, clientClassificationOptions, 
        		clientNonPersonConstitutionOptions, clientNonPersonMainBusinessLineOptions, clientLegalFormOptions,null,null, null);
    }
    
    
    
    
    
    
    
 private static final class LoansDetailsMapper implements RowMapper<LoanDetailData> {
    	
    	/*
    	 * No change in the query but need to add lot of functions in sql
    	 * 
    	 */
    	
    	public String schema() {
		
    		return	" select sum(a.openingarrears) as openingarrears, sum(a.arrearcollection) as arrearcollection,"
					+ " sum(a.currentdemand) as currentdemand,sum(a.currentcollection) as currentcollection,sum(a.closingarrears) as closingarrears,"
					+ " sum(a.advancecollection) as advancecollection from"
					+ " (select b.id,b.oa as 'openingarrears',if(b.oa<= b.ac,b.oa,b.ac) as 'arrearcollection',"
					+ " if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0))))as 'currentdemand',"
					+ " if(b.oa> b.ac,0,if((b.ac-b.oa)<if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))),b.ac-b.oa,if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))))) as 'currentcollection',"
					+ " if(b.adc < b.adc1,b.ac,(if(b.ac > (b.adc-b.adc1),b.ac-(b.adc-b.adc1),0))) as 'advancecollection',"
					+ " b.oa+if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0))))-if(b.oa<= b.ac,b.oa,b.ac) -if(b.oa> b.ac,0,if((b.ac-b.oa)<if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))),b.ac-b.oa,if(((b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))<0),0,(b.cd - (if(b.cd1 < (b.cd2),(b.cd2)-b.cd1,0)))))) as 'closingarrears' from"
					
					
					+ " (select ml.id as id,ifnull(getOpeningarrear(ml.id,?),0) as oa,ifnull(getArrearcollection(ml.id,?,DATE_ADD(DATE_ADD(MAKEDATE(year (?), 1),INTERVAL (SELECT month (?))-1 MONTH), INTERVAL (SELECT day(LAST_DAY (?)))-1 DAY)),0)as ac,"
					+ " ifnull(getCurrentdemand(ml.id,?,DATE_ADD(DATE_ADD(MAKEDATE(year (?), 1),INTERVAL (SELECT month (?))-1 MONTH), INTERVAL (SELECT day(LAST_DAY (?)))-1 DAY)),0) as cd,ifnull(getCurrentdemand1(ml.id,?),0) as cd1,"
					+ " ifnull(getCurrentdemand2(ml.id,?),0) as cd2,"
					+ " ifnull(getAdvancecollection(ml.id,DATE_ADD(DATE_ADD(MAKEDATE(year (?), 1),INTERVAL (SELECT month (?))-1 MONTH), INTERVAL (SELECT day(LAST_DAY (?)))-1 DAY)),0) as adc,"
					+ " ifnull(getAdvancecollection1(ml.id,?),0) as adc1"
					
					+ " from m_office mo "
					+ " JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(mo.hierarchy, '%' ) "
					+ " AND ounder.hierarchy like CONCAT('.', '%' ) "
					+ " LEFT JOIN m_client mc ON mc.office_id=ounder.id "
					+ " LEFT JOIN m_loan ml ON ml.client_id=mc.id AND ((ml.loan_status_id in (300,700) "
					+ " and ml.disbursedon_date <= DATE_ADD(DATE_ADD(MAKEDATE(year (?), 1),INTERVAL (SELECT month (?))-1 MONTH), INTERVAL (SELECT day(LAST_DAY (?)))-1 DAY)) or"
					+ " (ml.loan_status_id in (600,601,700) and ml.closedon_date >= DATE_ADD(MAKEDATE(year (?), 1),INTERVAL (SELECT month (?))-1 MONTH) ))"
					 
					
					+ " WHERE mo.id=? and  ml.id is not null)b)a;";
					

					 
					      
		}

		@Override
		public LoanDetailData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final long openingarrears = rs.getInt("openingarrears");
			final long currentdemand = rs.getInt("currentdemand");
			final long arrearcollection = rs.getInt("arrearcollection");
			final long advancecollection = rs.getInt("advancecollection");
			final long currentcollection = rs.getInt("currentcollection");
			final long closingarrears = rs.getInt("closingarrears");
			
			
			
			return new LoanDetailData(openingarrears,currentdemand,arrearcollection,advancecollection,currentcollection,closingarrears);
			
		}
    }


}