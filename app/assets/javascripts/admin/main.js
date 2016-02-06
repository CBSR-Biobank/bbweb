/**
 * Study configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular                                 = require('angular'),

      AdminCtrl                               = require('./AdminCtrl'),
      adminService                            = require('./adminService'),
      adminStates                             = require('./states'),
      pagedItemsListDirective                 = require('./pagedItemsListDirective'),
      statusLineDirective                     = require('./statusLineDirective'),

      CentreCtrl                              = require('./centres/CentreCtrl'),
      CentreEditCtrl                          = require('./centres/CentreEditCtrl'),
      CentreSummaryTabCtrl                    = require('./centres/CentreSummaryTabCtrl'),
      CentresCtrl                             = require('./centres/CentresCtrl'),
      centreStudiesPanelDirective             = require('./centres/centreStudiesPanelDirective'),
      locationsPanelDirective                 = require('./centres/locationsPanelDirective'),
      centreStates                            = require('./centres/states'),
      LocationEditCtrl                        = require('./centres/LocationEditCtrl'),

      StudiesCtrl                             = require('./studies/StudiesCtrl'),
      StudyCtrl                               = require('./studies/StudyCtrl'),
      StudyEditCtrl                           = require('./studies/StudyEditCtrl'),
      StudySummaryTabCtrl                     = require('./studies/StudySummaryTabCtrl'),
      studiesStates                           = require('./studies/states'),
      validAmountDirective                    = require('./studies/validAmountDirective'),
      validCountDirective                     = require('./studies/validCountDirective'),

      AnnotationTypeEditCtrl                  = require('./studies/annotationTypes/AnnotationTypeEditCtrl'),
      studyAnnotationTypeUtilsService         = require('./studies/annotationTypes/studyAnnotationTypeUtilsService'),
      studyAnnotationTypesPanelDirective      = require('./studies/annotationTypes/studyAnnotationTypesPanelDirective'),
      studyAnnotationTypesTableDirective      = require('./studies/annotationTypes/studyAnnotationTypesTableDirective'),

      CeventTypeEditCtrl                      = require('./studies/ceventTypes/CeventTypeEditCtrl'),
      ceventTypesPanelDirective               = require('./studies/ceventTypes/ceventTypesPanelDirective'),
      ceventTypesStates                       = require('./studies/ceventTypes/states'),

      participantsStates                      = require('./studies/participants/states'),

      ProcessingTypeEditCtrl                  = require('./studies/processing/ProcessingTypeEditCtrl'),
      SpcLinkTypeEditCtrl                     = require('./studies/processing/SpcLinkTypeEditCtrl'),
      processingTypesPanelDirective           = require('./studies/processing/processingTypesPanelDirective'),
      spcLinkTypesPanelDirective              = require('./studies/processing/spcLinkTypesPanelDirective'),
      processingStates                        = require('./studies/processing/states'),

      SpecimenGroupEditCtrl                   = require('./studies/specimenGroups/SpecimenGroupEditCtrl'),
      specimenGroupUtilsService               = require('./studies/specimenGroups/specimenGroupUtilsService'),
      specimenGroupsPanelDirective            = require('./studies/specimenGroups/specimenGroupsPanelDirective'),
      specimenGroupsStates                    = require('./studies/specimenGroups/states'),

      UsersTableCtrl                          = require('./users/UsersTableCtrl'),
      usersStates                             = require('./users/states');

  var module = angular.module('biobank.admin', [
    'biobank.common',
    'biobank.users',
    'biobank.studies'
  ]);

  module.controller('AdminCtrl', AdminCtrl);
  module.controller('LocationEditCtrl', LocationEditCtrl);

  module.service('adminService', adminService);

  module.config(adminStates);

  module.controller('CentreCtrl', CentreCtrl);
  module.controller('CentreEditCtrl', CentreEditCtrl);
  module.controller('CentreSummaryTabCtrl', CentreSummaryTabCtrl);
  module.controller('CentresCtrl', CentresCtrl);

  module.directive('centreStudiesPanel', centreStudiesPanelDirective);

  module.directive('locationsPanel', locationsPanelDirective);

  module.config(centreStates);

  module.directive('pagedItemsList', pagedItemsListDirective);
  module.directive('statusLine', statusLineDirective);

  module.controller('StudiesCtrl', StudiesCtrl);

  module.controller('StudyCtrl', StudyCtrl);
  module.controller('StudyEditCtrl', StudyEditCtrl);
  module.controller('StudySummaryTabCtrl', StudySummaryTabCtrl);

  module.controller('AnnotationTypeEditCtrl', AnnotationTypeEditCtrl);
  module.service('studyAnnotationTypeUtils', studyAnnotationTypeUtilsService);

  module.directive('studyAnnotationTypesPanel', studyAnnotationTypesPanelDirective);

  module.directive('studyAnnotationTypesTable', studyAnnotationTypesTableDirective);

  module.controller('CeventTypeEditCtrl', CeventTypeEditCtrl);

  module.directive('ceventTypesPanel', ceventTypesPanelDirective);

  module.config(ceventTypesStates);
  module.config(participantsStates);

  module.controller('ProcessingTypeEditCtrl', ProcessingTypeEditCtrl);

  module.controller('SpcLinkTypeEditCtrl', SpcLinkTypeEditCtrl);

  module.directive('processingTypesPanel', processingTypesPanelDirective);

  module.directive('spcLinkTypesPanel', spcLinkTypesPanelDirective);

  module.config(processingStates);

  module.controller('SpecimenGroupEditCtrl', SpecimenGroupEditCtrl);

  module.service('specimenGroupUtils', specimenGroupUtilsService);

  module.directive('specimenGroupsPanel', specimenGroupsPanelDirective);

  module.config(specimenGroupsStates);

  module.config(studiesStates);

  module.directive('validAmount', validAmountDirective);

  module.directive('validCount', validCountDirective);

  module.controller('UsersTableCtrl', UsersTableCtrl);

  module.config(usersStates);

  return module;
});
