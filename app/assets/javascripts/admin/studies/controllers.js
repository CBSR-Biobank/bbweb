/**
 * Study administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', [
    'biobank.common',
    'studies.services',
    'admin.studies.helpers',
    'admin.studies.participants.helpers'
  ]);

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * 'user' is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesCtrl', [
    '$rootScope', '$scope', '$state', '$log', 'StudyService',
    function($rootScope, $scope, $state, $log, StudyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      StudyService.list().then(
        function(studies) {
          $scope.studies = _.sortBy(studies, function(study) { return study.name; });
        });
    }
  ]);

  /**
   * Displays a list of studies in an ng-table.
   */
  mod.controller('StudiesTableCtrl', [
    '$scope', '$rootScope', '$filter', '$state', 'ngTableParams', 'StudyService',
    function($scope, $rootScope, $filter, $state, ngTableParams, StudyService) {

      var updateData = function() {
        StudyService.list().then(function(data) {
          $scope.studies = data;
          $scope.tableParams.reload();
        });
      };

      var getTableData = function() {
        return $scope.studies;
      };

      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      /* jshint -W055 */
      $scope.tableParams = new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      }, {
        counts: [], // hide page counts control
        total: function () { return getTableData().length; },
        getData: function($defer, params) {
          var filteredData = getTableData();
          var orderedData = params.sorting() ?
              $filter('orderBy')(filteredData, params.orderBy()) : $scope.studies;
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        }
      });
      /* jshint +W055 */

      $scope.tableParams.settings().$scope = $scope;
      updateData();
    }
  ]);

  /**
   * Called to add a study.
   */
  mod.controller('StudyAddCtrl', [
    '$scope', '$state', 'studyEditService', 'user', 'study',
    function($scope, $state, studyEditService, user, study) {
      $scope.title =  'Add new study';
      $scope.study = study;

      var callback = function () {
        $state.go('admin.studies');
      };

      studyEditService.edit($scope, callback, callback, callback);
    }
  ]);

  /**
   * Called to update the summary information for study.
   */
  mod.controller('StudyUpdateCtrl', [
    '$scope', '$state', 'studyEditService', 'user', 'study',
    function($scope, $state, studyEditService, user, study) {
      $scope.title = 'Update study';
      $scope.study = study;

      var callback = function () {
        // the state we go to needs to reload the study since it has changed
        $state.go('admin.studies.study', { studyId: $scope.study.id }, { reload: true });
      };

      studyEditService.edit($scope, callback, callback, callback);
    }
  ]);

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   */
  mod.controller('StudySummaryTabCtrl', [
    '$scope', '$state', '$filter', 'user', 'study',
    function($scope, $state, $filter, user, study) {
      $scope.study = study;
      $scope.description = $scope.study.description;
      $scope.descriptionToggle = true;
      $scope.descriptionToggleLength = 100;

      $scope.updateStudy = function(study) {
        if (study.id) {
          $state.go('admin.studies.study.update', { studyId: study.id });
          return;
        }
        throw new Error('study does not have an ID');
      };

      $scope.changeStatus = function(study) {
        if (study.id) {
          alert('change status of ' + study.name);
          return;
        }
        throw new Error('study does not have an ID');
      };

      $scope.truncateDescriptionToggle = function() {
        if ($scope.descriptionToggle) {
          $scope.description = $filter('truncate')(
            $scope.study.description, $scope.descriptionToggleLength);
        } else {
          $scope.description = $scope.study.description;
        }
        $scope.descriptionToggle = !$scope.descriptionToggle;
      };
    }
  ]);

  /**
   * Displays study participant information in a table.
   */
  mod.controller('ParticipantsTabCtrl', [
    '$rootScope',
    '$scope',
    '$state',
    '$timeout',
    'AnnotTypesPanelSettings',
    'participantAnnotTypeRemoveService',
    'annotTypes',
    function ($rootScope,
              $scope,
              $state,
              $timeout,
              AnnotTypesPanelSettings,
              participantAnnotTypeRemoveService,
              annotTypes) {
      $scope.panel = {
        annotTypes: new AnnotTypesPanelSettings(
          'participantAnnotTypes',
          annotTypes,
          'Participant Annotation Types',
          'Participant annotations allow a study to collect custom named and ' +
            'defined pieces of data for each participant. Annotations are optional and are not ' +
            'required to be defined.',
          true,
          function() {
            $state.go('admin.studies.study.participants.annotTypeAdd');
          },
          function(annotType) {
            $state.go('admin.studies.study.participants.annotTypeUpdate',
                      { annotTypeId: annotType.id });
          },
          participantAnnotTypeRemoveService.remove
        )
      };

      $rootScope.$emit('studyTabChanged', 'participantTab');
    }
  ]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensTabCtrl', [
    '$scope', 'SpecimenGroupsPanelSettings', 'specimenGroups',
    function($scope, SpecimenGroupsPanelSettings, specimenGroups) {
      $scope.panel = {
        specimenGroups: new SpecimenGroupsPanelSettings('specimenGroups', specimenGroups)
      };
    }
  ]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('CollectionTabCtrl', [
    '$scope',
    '$state',
    'AnnotTypesPanelSettings',
    'CeventTypesPanelSettings',
    'ceventAnnotTypeRemoveService',
    'ceventTypes',
    'annotTypes',
    'specimenGroups',
    function($scope,
             $state,
             AnnotTypesPanelSettings,
             CeventTypesPanelSettings,
             ceventAnnotTypeRemoveService,
             ceventTypes,
             annotTypes,
             specimenGroups) {

      $scope.panel = {
        ceventTypes: new CeventTypesPanelSettings(
          'ceventTypes', ceventTypes, specimenGroups, annotTypes),
        annotTypes: new AnnotTypesPanelSettings(
          'ceventAnnotTypes',
          annotTypes,
          'Collection Event Annotation Types',
          'Collection event annotations allow a study to collect custom named and defined ' +
            'pieces of data for each collection event. Annotations are optional and are not ' +
            'required to be defined.',
          false,
          function() {
            $state.go('admin.studies.study.collection.ceventAnnotTypeAdd');
          },
          function(annotType) {
            $state.go('admin.studies.study.collection.ceventAnnotTypeUpdate',
                      { annotTypeId: annotType.id });
          },
          ceventAnnotTypeRemoveService.remove
        )
      };
    }
  ]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('ProcessingTabCtrl', [
    '$scope',
    '$state',
    'AnnotTypesPanelSettings',
    'spcLinkAnnotTypeRemoveService',
    'ProcessingTypesPanelSettings',
    'SpcLinkTypesPanelSettings',
    'dtoProcessing',
    function($scope,
             $state,
             AnnotTypesPanelSettings,
             spcLinkAnnotTypeRemoveService,
             ProcessingTypesPanelSettings,
             SpcLinkTypesPanelSettings,
             dtoProcessing) {

      var processingTypesById = _.indexBy(dtoProcessing.processingTypes, 'id');
      var specimenGroupsById = _.indexBy(dtoProcessing.specimenGroups, 'id');
      var annotTypesById = _.indexBy(dtoProcessing.specimenLinkAnnotationTypes, 'id');

      var tableData = [];
      dtoProcessing.specimenLinkTypes.forEach(function(slt) {
        var annotationTypes = [];
        slt.annotationTypeData.forEach(function (annotTypeItem) {
          var at = annotTypesById[annotTypeItem.annotationTypeId];
          annotationTypes.push({id: annotTypeItem.annotationTypeId, name: at.name });
        });

        tableData.push({
          specimenLinkType: slt,
          processingTypeName: processingTypesById[slt.processingTypeId].name,
          inputGroupName:     specimenGroupsById[slt.inputGroupId].name,
          outputGroupName:    specimenGroupsById[slt.outputGroupId].name,
          annotationTypes:    annotationTypes
        });
      });

      $scope.panel = {
        processingTypes: new ProcessingTypesPanelSettings(
          'processingTypes', dtoProcessing.processingTypes),
        spcLinkTypes: new SpcLinkTypesPanelSettings(
          'spcLinkTypes', processingTypesById, specimenGroupsById, annotTypesById, tableData),
        annotTypes: new AnnotTypesPanelSettings(
          'spcLinkAnnotTypes',
          dtoProcessing.specimenLinkAnnotationTypes,
          'Specimen Link Annotation Types',
          'Specimen link annotations allow a study to collect custom named and defined ' +
            'pieces of data when processing specimens. Annotations are optional and are not ' +
            'required to be defined.',
          false,
          function() {
            $state.go('admin.studies.study.processing.spcLinkAnnotTypeAdd');
          },
          function(annotType) {
            $state.go('admin.studies.study.processing.spcLinkAnnotTypeUpdate',
                      { annotTypeId: annotType.id });
          },
          spcLinkAnnotTypeRemoveService.remove
        )
      };
    }
  ]);

  return mod;
});
