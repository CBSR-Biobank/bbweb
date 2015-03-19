/** Study service */
define(function (require) {
  'use strict';

  var angular                                 = require('angular'),

      AdminCtrl                               = require('./AdminCtrl'),
      LocationEditCtrl                        = require('./LocationEditCtrl'),
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

      StudiesCtrl                             = require('./studies/StudiesCtrl'),
      StudyCtrl                               = require('./studies/StudyCtrl'),
      StudyEditCtrl                           = require('./studies/StudyEditCtrl'),
      StudySummaryTabCtrl                     = require('./studies/StudySummaryTabCtrl'),
      studiesStates                           = require('./studies/states'),
      studyViewSettingsService                = require('./studies/studyViewSettingsService'),
      validAmountDirective                    = require('./studies/validAmountDirective'),
      validCountDirective                     = require('./studies/validCountDirective'),

      AnnotationTypeEditCtrl                  = require('./studies/annotationTypes/AnnotationTypeEditCtrl'),
      annotationTypeRemoveService             = require('./studies/annotationTypes/annotationTypeRemoveService'),
      ceventAnnotTypesPanelDirective          = require('./studies/annotationTypes/ceventAnnotTypesPanelDirective'),
      participantAnnotTypesPanelDirective     = require('./studies/annotationTypes/participantAnnotTypesPanelDirective'),
      spcLinkAnnotTypeRemoveService           = require('./studies/annotationTypes/spcLinkAnnotTypeRemoveService'),

      spcLinkAnnotTypesPanelDirective         = require('./studies/annotationTypes/spcLinkAnnotTypesPanelDirective'),
      CeventTypeEditCtrl                      = require('./studies/ceventTypes/CeventTypeEditCtrl'),
      ceventTypeRemoveService                 = require('./studies/ceventTypes/ceventTypeRemoveService'),
      ceventTypesPanelDirective               = require('./studies/ceventTypes/ceventTypesPanelDirective'),
      ceventTypesStates                       = require('./studies/ceventTypes/states'),

      participantsStates                      = require('./studies/participants/states'),

      ProcessingTypeEditCtrl                  = require('./studies/processing/ProcessingTypeEditCtrl'),
      SpcLinkTypeEditCtrl                     = require('./studies/processing/SpcLinkTypeEditCtrl'),
      processingTypeRemoveService             = require('./studies/processing/processingTypeRemoveService'),
      processingTypesPanelDirective           = require('./studies/processing/processingTypesPanelDirective'),
      spcLinkTypeRemoveService                = require('./studies/processing/spcLinkTypeRemoveService'),
      spcLinkTypesPanelDirective              = require('./studies/processing/spcLinkTypesPanelDirective'),
      processingStates                        = require('./studies/processing/states'),

      SpecimenGroupEditCtrl                   = require('./studies/specimenGroups/SpecimenGroupEditCtrl'),
      specimenGroupRemoveService              = require('./studies/specimenGroups/specimenGroupRemoveService'),
      specimenGroupsPanelDirective            = require('./studies/specimenGroups/specimenGroupsPanelDirective'),
      specimenGroupsStates                    = require('./studies/specimenGroups/states'),

      UsersTableCtrl                          = require('./users/UsersTableCtrl'),
      usersStates                             = require('./users/states');

  var module = angular.module('biobank.admin', [
    'ui.router',
    'ngSanitize',
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

  module.directive('centreStudiesPanel', centreStudiesPanelDirective.directive);
  module.controller('CentreStudiesPanelCtrl', centreStudiesPanelDirective.controller);

  module.directive('locationsPanel', locationsPanelDirective.directive);
  module.controller('LocationsPanelCtrl', locationsPanelDirective.controller);

  module.config(centreStates);

  module.directive('pagedItemsList', pagedItemsListDirective.directive);
  module.controller('PagedItemsListCtrl', pagedItemsListDirective.controller);

  module.directive('statusLine', statusLineDirective);

  module.controller('StudiesCtrl', StudiesCtrl);

  module.controller('StudyCtrl', StudyCtrl);
  module.controller('StudyEditCtrl', StudyEditCtrl);
  module.controller('StudySummaryTabCtrl', StudySummaryTabCtrl);

  module.controller('AnnotationTypeEditCtrl', AnnotationTypeEditCtrl);
  module.service('annotationTypeRemoveService', annotationTypeRemoveService);

  module.directive('ceventAnnotTypesPanel', ceventAnnotTypesPanelDirective.directive);
  module.controller('CeventAnnotTypesPanelCtrl', ceventAnnotTypesPanelDirective.controller);

  module.directive('participantsAnnotTypesPanel', participantAnnotTypesPanelDirective.directive);
  module.controller('ParticipantAnnotTypesPanelCtrl', participantAnnotTypesPanelDirective.controller);

  module.service('spcLinkAnnotTypeRemoveService', spcLinkAnnotTypeRemoveService);

  module.directive('spcLinkAnnotTypesPanel', spcLinkAnnotTypesPanelDirective.directive);
  module.controller('SpcLinkAnnotTypesPanelCtrl', spcLinkAnnotTypesPanelDirective.controller);

  module.controller('CeventTypeEditCtrl', CeventTypeEditCtrl);

  module.service('ceventTypeRemoveService', ceventTypeRemoveService);

  module.directive('ceventTypesPanel', ceventTypesPanelDirective.directive);
  module.controller('CeventTypesPanelCtrl', ceventTypesPanelDirective.controller);

  module.config(ceventTypesStates);
  module.config(participantsStates);

  module.controller('ProcessingTypeEditCtrl', ProcessingTypeEditCtrl);

  module.controller('SpcLinkTypeEditCtrl', SpcLinkTypeEditCtrl);

  module.service('processingTypeRemoveService', processingTypeRemoveService);

  module.directive('processingTypesPanel', processingTypesPanelDirective.directive);
  module.controller('ProcessingTypesPanelCtrl', processingTypesPanelDirective.controller);

  module.service('spcLinkTypeRemoveService', spcLinkTypeRemoveService);

  module.directive('spcLinkTypesPanel', spcLinkTypesPanelDirective.directive);
  module.controller('SpcLinkTypesPanelCtrl', spcLinkTypesPanelDirective.controller);

  module.config(processingStates);

  module.controller('SpecimenGroupEditCtrl', SpecimenGroupEditCtrl);

  module.service('specimenGroupRemoveService', specimenGroupRemoveService);

  module.directive('specimenGroupsPanel', specimenGroupsPanelDirective.directive);
  module.controller('SpecimenGroupsPanelCtrl', specimenGroupsPanelDirective.controller);

  module.config(specimenGroupsStates);

  module.config(studiesStates);

  module.service('studyViewSettingsService', studyViewSettingsService);

  module.directive('validAmount', validAmountDirective);

  module.directive('validCount', validCountDirective);

  module.controller('UsersTableCtrl', UsersTableCtrl);

  module.config(usersStates);

  return module;
});
