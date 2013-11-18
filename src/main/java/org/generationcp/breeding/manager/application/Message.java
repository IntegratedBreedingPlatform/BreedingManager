package org.generationcp.breeding.manager.application;


public enum Message{
    MAIN_WINDOW_CAPTION
    ,BACK
    ,DONE
    ,OK
    ,SUCCESS
    ,ERROR_INTERNAL
    ,ERROR_PLEASE_CONTACT_ADMINISTRATOR
    ,ERROR_MUST_BE_SPECIFIED
    ,GERMPLASM_BREEDING_METHOD_LABEL
    ,GERMPLASM_DATE_LABEL
    ,GERMPLASM_DETAILS_LABEL
    ,GERMPLASM_LOCATION_LABEL
    ,GERMPLASM_NAME_TYPE_LABEL
    ,I_WANT_TO_IMPORT_GERMPLASM_LIST
    ,IMPORT_GERMPLASM_LIST_TAB_LABEL
    ,IMPORT_PEDIGREE_OPTION_ONE
    ,IMPORT_PEDIGREE_OPTION_TWO
    ,IMPORT_PEDIGREE_OPTION_THREE
    ,LIST_DATE_LABEL
    ,LIST_DESCRIPTION_LABEL
    ,LIST_NAME_LABEL
    ,LIST_TYPE_LABEL
    ,NEXT
    ,OPEN_GERMPLASM_IMPORT_FILE
    ,PEDIGREE_OPTIONS_LABEL
    ,SAVE_GERMPLASM_LIST
    ,SELECT_GERMPLASM_LIST_FILE
    ,SPECIFY_GERMPLASM_DETAILS
    ,UPLOAD
    ,WELCOME_LABEL
    ,WELCOME_QUESTION_LABEL
    ,WELCOME_TAB_LABEL
    ,CROSSING_MANAGER_LABEL
    ,CROSSING_MANAGER_TAB_LABEL
    ,I_WANT_TO_IMPORT_CROSSING_MANAGER_DATA
    ,SELECT_NURSERY_TEMPLATE
    ,MAKE_CROSSES
    ,ENTER_ADDITIONAL_DETAILS_OF_GERMPLASM_RECORDS_FOR_CROSSES
    ,ENTER_DETAILS_FOR_LIST_OF_CROSS
    ,SELECT_NURSERY_TEMPLATE_FILE
    ,SELECT_AN_OPTION_FOR_SPECIFYING_CROSSES
    ,I_HAVE_ALREADY_DEFINED_CROSSES_IN_THE_NURSERY_TEMPLATE_FILE
    ,I_WANT_TO_MANUALLY_MAKE_CROSSES
    ,UPLOADED_FILE
    
    //Select Germplasm List
    ,CONFIRM_RECORDS_WILL_BE_SAVED_FOR_GERMPLASM
    ,GERMPLASM_LIST_SAVED_SUCCESSFULLY
    ,MAKE_NEW_IMPORT
    ,CONFIRM_REDIRECT_TO_IMPORT_WIZARD
    ,I_WANT_TO_SELECT_GERMPLASM_LIST
    ,DB_LOCAL_TEXT
    ,DB_CENTRAL_TEXT
    ,GERMPLASM_LIST_DETAILS_LABEL
    ,REFRESH_LABEL
    ,CANCEL_LABEL
    ,DONE_LABEL
    ,SELECTED_LIST_LABEL
    ,DESCRIPTION_LABEL
    ,LIST_ENTRIES_LABEL
    ,LISTDATA_GID_HEADER
    ,LISTDATA_ENTRY_ID_HEADER
    ,LISTDATA_ENTRY_CODE_HEADER
    ,LISTDATA_SEEDSOURCE_HEADER
    ,LISTDATA_DESIGNATION_HEADER
    ,LISTDATA_GROUPNAME_HEADER
    ,LISTDATA_STATUS_HEADER
    ,ERROR_DATABASE
    ,ERROR_IN_GETTING_TOP_LEVEL_FOLDERS
    ,ERROR_INVALID_FORMAT
    ,ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW
    ,ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID
    ,ERROR_IN_NUMBER_FORMAT
    ,ERROR_IN_GETTING_LAST_SELECTED_LIST
    
    //Crossing Manager Additional Details
    ,SELECT_AN_OPTION
    ,CROSSING_METHOD
    ,SELECT_CROSSING_METHOD
    ,METHOD_DESCRIPTION_LABEL
    ,CROSSING_METHOD_WILL_BE_THE_SAME_FOR_ALL_CROSSES
    ,CROSSING_METHOD_WILL_BE_SET_BASED_ON_STATUS_OF_PARENTAL_LINES
    ,CROSS_CODE
    ,USE_DEFAULT_CROSS_CODE_FOR_ALL
    ,SPECIFY_CROSS_CODE_TEMPLATE_FOR_ALL
    ,SPECIFY_PREFIX_REQUIRED
    ,SEQUENCE_NUMBER_SHOULD_HAVE_LEADING_ZEROS
    ,HOW_MANY_DIGITS
    ,SPECIFY_SUFFIX_OPTIONAL
    ,THE_NEXT_NAME_IN_THE_SEQUENCE_WILL_BE
    ,GENERATE
    ,CROSS_INFO
    ,HARVEST_DATE
    ,HARVEST_LOCATION
    ,ERROR_IN_GETTING_NEXT_NUMBER_IN_CROSS_NAME_SEQUENCE
    ,ERROR_ENTER_PREFIX_FIRST
    ,ERROR_PREFIX_HAS_WHITESPACE
    ,ERROR_IN_GETTING_BREEDING_METHOD_BASED_ON_PARENTAL_LINES
    ,ERROR_NEXT_NAME_MUST_BE_GENERATED_FIRST
    
    ,GERMPLASM_LIST_NAME
    ,GERMPLASM_LIST_DESCRIPTION
    ,GERMPLASM_LIST_TYPE
    ,GERMPLASM_LIST_DATE
    
    //Saving Crosses action
    ,MAKE_NEW_CROSSES
    ,EXPORT_CROSSES_MADE
    ,SAVE_CROSSES_MADE
    ,CONFIRM_RECORDS_WILL_BE_SAVED_FOR_CROSSES_MADE
    ,CONFIRM_REDIRECT_TO_MAKE_CROSSES_WIZARD
    ,ERROR_IN_SAVING_CROSSES_DEFINED
    ,CROSSES_SAVED_SUCCESSFULLY
    
    //Make Cross Screen
    ,LABEL_FEMALE_PARENTS
    ,LABEL_MALE_PARENTS
    ,LABEL_FEMALE_PARENT
    ,LABEL_MALE_PARENT
    ,SELECT_FEMALE_LIST_BUTTON_LABEL
    ,SELECT_MALE_LIST_BUTTON_LABEL
    ,MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL
    ,MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL
    ,MAKE_CROSSES_CHECKBOX_LABEL
    ,MAKE_CROSSES_BUTTON_LABEL
    ,LABEL_CROSS_MADE
    ,ERROR_CROSS_MUST_BE_SELECTED
    ,ERROR_IN_GETTING_CROSSING_NAME_TYPE
    ,ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL
    ,ERROR_GERMPLASM_LIST_IMPORT_BOTH_ID_REQUIRED
    ,ERROR_GERMPLASM_LIST_IMPORT_MALE_ID_REQUIRED
    ,ERROR_GERMPLASM_LIST_IMPORT_FEMALE_ID_REQUIRED
    ,INVALID_NURSERY_TEMPLATE_FILE
    ,NUMBER
    
    //Nursery Template Screens
    ,I_WANT_TO_WRITE_NURSERY_TEMPLATE_FILES
    ,NURSERY_TEMPLATE_TAB_LABEL
    ,NURSERY_TEMPLATE_CAPTION_LABEL
    ,SPECIFY_NURSERY_CONDITIONS_LABEL
    ,CONDITION_HEADER
    ,DESCRIPTION_HEADER
    ,PROPERTY_HEADER
    ,SCALE_HEADER
    ,VALUE_HEADER 
    ,INVALID_SITE_ID
    ,INVALID_BREEDER_ID
    ,INVALID_METHOD_ID
    ,PARENTAGE
    ,CONFIRM_DIALOG_CAPTION_EXPORT_NURSERY_FILE
    ,CONFIRM_DIALOG_MESSAGE_EXPORT_NURSERY_FILE
    ,ADD_SPACE_BETWEEN_PREFIX_AND_CODE
    ,SELECT_A_METHOD
    ,CROSS_NAME
    ,SEQUENCE_NUMBER_SHOULD_HAVE
    ,LEADING_ZEROS
    ,DIGITS
    ,PLEASE_SELECT_A_GERMPLASM_FROM_THE_TABLE
    ,BY_CLICKING_ON_THE_DONE_BUTTON
    ,START_NEW_IMPORT
    ,CLICKING_ON_DONE_WOULD_MEAN_THE_LIST_LIST_ENTRIES_AND_GERMPLASM_RECORDS_WILL_BE_SAVED_IN_THE_DATABASE
    
    //List Manager Screens
    ,LIST_MANAGER_TAB_LABEL
    ,LIST_MANAGER_SCREEN_LABEL
    ,BROWSE_LISTS
    ,REVIEW_LIST_DETAILS
    ,SEARCH_LISTS_AND_GERMPLASM
    ,LIST_DETAILS
    ,LIST_DATA
    ,LIST_SEED_INVENTORY
    ,CLOSE_ALL_TABS
    
    //List Manager Screen: List Details
    ,NAME_LABEL
    ,CREATION_DATE_LABEL
    ,TYPE_LABEL
    ,STATUS_LABEL
    ,LIST_OWNER_LABEL
    ,ERROR_MONTH_OUT_OF_RANGE
    ,ERROR_DAY_OUT_OF_RANGE
    ,SEARCH_FOR
    ,MATCHING_LISTS
    ,SELECT_A_LIST_TO_VIEW_THE_DETAILS
    ,MATCHING_GERMPLASM
    ,SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS
    ,BUILD_A_NEW_LIST
    ,BUILD_YOUR_LIST_BY_DRAGGING_LISTS_OR_GERMPLASM_RECORDS_INTO_THIS_NEW_LIST_WINDOW
    ,DATE_LABEL
    ,NOTES
    ,SAVE_LIST
    
}
