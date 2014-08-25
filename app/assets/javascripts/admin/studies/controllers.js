/**
 * Study administration controllers.
 */
define(['angular', 'underscore', 'common'], function(angular, _, common) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', ['studies.services']);

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesCtrl', [
    '$rootScope', '$scope', '$state', '$log', 'StudyService',
    function($rootScope, $scope, $state, $log, StudyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      StudyService.list().then(
        function(data) {
          $scope.studies = _.sortBy(data, function(study) { return study.name; });
        });

      $scope.addStudy = function() {
        $state.go('admin.studies.add');
      };

      $scope.studyInformation = function(study) {
        $state.go('admin.studies.study', { studyId: study.id });
      };

      $scope.tableView = function() {
        $state.go('admin.studies.table');
      };
    }]);

  /**
   * Displays a list of studies in an ng-table.
   */
  mod.controller('StudiesTableCtrl', [
    '$scope', '$rootScope', '$filter', '$state', 'ngTableParams', 'StudyService',
    function($scope, $rootScope, $filter, $state, ngTableParams, StudyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      StudyService.list().then(function(data) {
        $scope.studies = data;

        /* jshint ignore:start */
        $scope.tableParams = new ngTableParams({
          page: 1,            // show first page
          count: 10,          // count per page
          sorting: {
            name: 'asc'       // initial sorting
          }
        }, {
          counts: [], // hide page counts control
          total: $scope.studies.length,
          getData: function($defer, params) {
            var orderedData = params.sorting()
              ? $filter('orderBy')($scope.studies, params.orderBy())
              : $scope.studies;
            $defer.resolve(orderedData.slice(
              (params.page() - 1) * params.count(),
              params.page() * params.count()));
          }
        });
        /* jshint ignore:end */
      });

      $scope.addStudy = function() {
        $state.go("admin.studies.add");
      };

      $scope.studyInformation = function(study) {
        $state.go("admin.studies.study.summary", { studyId: study.id });
      };

      $scope.defaultView = function() {
        $state.go("admin.studies");
      };

    }]);

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   */
  mod.controller('StudySummaryTabCtrl', [
    '$scope', '$rootScope', '$state', '$filter', 'user', 'study',
    function($scope, $rootScope, $state, $filter, user, study) {

      $scope.study = study;
      $scope.description = $scope.study.description;
      $scope.descriptionToggle = true;
      $scope.descriptionToggleLength = 100;

      $scope.updateStudy = function(study) {
        if (study.id) {
          $state.go("admin.studies.study.update", { studyId: study.id });
          return;
        }
        throw new Error("study does not have an ID");
      };

      $scope.changeStatus = function(study) {
        if (study.id) {
          alert("change status of " + study.name);
          return;
        }
        throw new Error("study does not have an ID");
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

    }]);

  /**
   * Called to add a study.
   */
  mod.controller('StudyAddCtrl', [
    '$scope', '$state', 'studyEditService', 'user', 'study',
    function($scope, $state, studyEditService, user, study) {
      $scope.title =  "Add new study";
      $scope.study = study;

      var callback = function () {
        $state.go('admin.studies');
      };

      studyEditService.edit($scope, callback, callback, callback);
    }]);

  /**
   * Called to update the summary information for study.
   */
  mod.controller('StudyUpdateCtrl', [
    '$scope', '$state', 'studyEditService', 'user', 'study',
    function($scope, $state, studyEditService, user, study) {
      $scope.title = "Update study";
      $scope.study = study;

      var callback = function () {
        // the state we go to needs to reload the study since it has changed
        $state.go('admin.studies.study', { studyId: $scope.study.id }, { reload: true });
      };

      studyEditService.edit($scope, callback, callback, callback);
    }]);

  /**
   * Displays study participant information in a table.
   */
  mod.controller('ParticipantsTabCtrl', [
    '$scope',
    '$state',
    'studyViewSettings',
    'AnnotTypesPanelSettings',
    'participantAnnotTypeRemoveService',
    'annotTypes',
    function (
      $scope,
      $state,
      studyViewSettings,
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
          function(annotType) {
            $state.go('admin.studies.study.participants.annotTypeAdd');
          },
          function(annotType) {
            $state.go('admin.studies.study.participants.annotTypeUpdate',
                      { annotTypeId: annotType.id });
          },
          participantAnnotTypeRemoveService
        )
      };
    }]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensTabCtrl', [
    '$injector',
    '$scope',
    '$state',
    'studyViewSettings',
    'PanelSettings',
    'specimenGroupModalService',
    'specimenGroupRemoveService',
    'specimenGroups',
    function(
      $injector,
      $scope,
      $state,
      studyViewSettings,
      PanelSettings,
      specimenGroupModalService,
      specimenGroupRemoveService,
      specimenGroups) {

      function SpecimenGroupSettings() {
        this.title = 'Specimen Groups';
        this.header = ' A Specimen Group is used to configure a specimen type to be used by the study. ' +
          'It records ownership, summary, storage, and classification information that applies ' +
          'to an entire group or collection of Specimens.';
        this.information = function(specimenGroup) {
          specimenGroupModalService.show(specimenGroup);
        };
        this.add = function(study) {
          $state.go('admin.studies.study.specimens.groupAdd');
        };
        this.update = function(specimenGroup) {
          $state.go('admin.studies.study.specimens.groupUpdate', { specimenGroupId: specimenGroup.id });
        };
        this.remove = function(specimenGroup) {
          specimenGroupRemoveService.remove(specimenGroup);
        };

        $injector.invoke(PanelSettings, this, {
          data: specimenGroups,
          panelStateName: 'specimenGroups'
        });
      }

      $scope.panel = {
        specimenGroups: new SpecimenGroupSettings()
      };
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('CollectionTabCtrl', [
    '$injector',
    '$scope',
    '$state',
    'PanelSettings',
    'AnnotTypesPanelSettings',
    'studyViewSettings',
    'ceventTypeModalService',
    'ceventTypeRemoveService',
    'ceventAnnotTypeRemoveService',
    'annotTypeModalService',
    'ceventTypes',
    'annotTypes',
    'specimenGroups',
    function(
      $injector,
      $scope,
      $state,
      PanelSettings,
      AnnotTypesPanelSettings,
      studyViewSettings,
      ceventTypeModalService,
      ceventTypeRemoveService,
      ceventAnnotTypeRemoveService,
      annotTypeModalService,
      ceventTypes,
      annotTypes,
      specimenGroups) {

      $scope.panel = {
        annotTypes: new AnnotTypesPanelSettings(
          'ceventAnnotTypes',
          annotTypes,
          'Collection Event Annotation Types',
          'Collection event annotations allow a study to collect custom named and defined ' +
            'pieces of data for each collection event. Annotations are optional and are not ' +
            'required to be defined.',
          false,
          function(annotType) {
            $state.go('admin.studies.study.collection.ceventAnnotTypeAdd');
          },
          function(annotType) {
            $state.go('admin.studies.study.collection.ceventAnnotTypeUpdate',
                      { annotTypeId: annotType.id });
          },
          ceventAnnotTypeRemoveService
        )
      };

      function CeventTypesPanelSettings($injector, $scope, ceventTypes) {
        this.title = 'Collection Event Types';
        this.header = 'A Collection Event Type defines a classification name, unique to the Study, to a ' +
          'participant visit. A participant visit is a record of when specimens were collected ' +
          'from a participant at a collection centre.';
        this.information = function(ceventType) {
          ceventTypeModalService.show(ceventType, specimenGroups, annotTypes);
        };
        this.add = function(study) {
          $state.go('admin.studies.study.collection.ceventTypeAdd', { studyId: study.id });
        };
        this.update = function(ceventType) {
          $state.go('admin.studies.study.collection.ceventTypeUpdate',
                    { studyId: ceventType.studyId, ceventTypeId: ceventType.id });
        };
        this.remove = function(ceventType) {
          ceventTypeRemoveService.remove(ceventType);
        };
        this.panelOpen = studyViewSettings.panelState('ceventTypes');
        this.panelToggle = function() {
          studyViewSettings.panelState('ceventTypes');
        };

        // push all specimen groups names into an array for easy display
        var specimenGroupsById = _.indexBy(specimenGroups, 'id');
        ceventTypes.forEach(function (cet) {
          cet.sgNames = [];
          cet.specimenGroupData.forEach(function (sgItem) {
            cet.sgNames.push(specimenGroupsById[sgItem.specimenGroupId].name);
          });
        });

        $injector.invoke(PanelSettings, this, {
          data: ceventTypes,
          panelStateName: 'ceventTypes'
        });
      }

      $scope.panel.ceventTypes = new CeventTypesPanelSettings($injector, $scope, ceventTypes);
    }]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('ProcessingTabCtrl', [
    '$injector',
    '$scope',
    '$state',
    'PanelSettings',
    'AnnotTypesPanelSettings',
    'studyViewSettings',
    'processingTypeModalService',
    'processingTypeRemoveService',
    'annotTypeModalService',
    'spcLinkAnnotTypeRemoveService',
    'spcLinkTypeModalService',
    'spcLinkTypeRemoveService',
    'specimenGroupModalService',
    'dtoProcessing',
    function($injector,
             $scope,
             $state,
             PanelSettings,
             AnnotTypesPanelSettings,
             studyViewSettings,
             processingTypeModalService,
             processingTypeRemoveService,
             annotTypeModalService,
             spcLinkAnnotTypeRemoveService,
             spcLinkTypeModalService,
             spcLinkTypeRemoveService,
             specimenGroupModalService,
             dtoProcessing) {

      var panelSettings = new AnnotTypesPanelSettings(
        'spcLinkAnnotTypes',
        dtoProcessing.specimenLinkAnnotationTypes,
        'Specimen Link Annotation Types',
        'Specimen link annotations allow a study to collect custom named and defined ' +
          'pieces of data when processing specimens. Annotations are optional and are not ' +
          'required to be defined.',
        false,
        function(annotType) {
          $state.go('admin.studies.study.processing.spcLinkAnnotTypeAdd');
        },
        function(annotType) {
          $state.go('admin.studies.study.processing.spcLinkAnnotTypeUpdate',
                    { annotTypeId: annotType.id });
        },
        spcLinkAnnotTypeRemoveService
      );

      $scope.panel = {
        annotTypes: panelSettings
      };

      function ProcessingTypesPanelSettings() {
        this.title = 'Processing Types';
        this.header = 'A Processing Type describes a regularly performed specimen processing procedure ' +
          'with a unique name (unique to this study). There should be one or more associated ' +
          'Specimen Link Types that (1) further define legal procedures and (2) allow recording ' +
          'of procedures performed on different types of Specimens. ';
        this.information = function(processingType) {
          processingTypeModalService.show(processingType);
        };
        this.add = function(study) {
          $state.go('admin.studies.study.processing.processingTypeAdd', { studyId: study.id });
        };
        this.update = function(processingType) {
          $state.go('admin.studies.study.processing.processingTypeUpdate',
                    { studyId: processingType.studyId, processingTypeId: processingType.id });
        };
        this.remove = function(processingType) {
          processingTypeRemoveService.remove(processingType);
        };
        this.panelOpen = studyViewSettings.panelState('processingTypes');
        this.panelToggle = function() {
          studyViewSettings.panelState('processingTypes');
        };

        $injector.invoke(PanelSettings, this, {
          data: dtoProcessing.processingTypes,
          panelStateName: 'processingTypes'
        });
      }

      $scope.panel.processingTypes = new ProcessingTypesPanelSettings();

      function SpcLinkTypesPanelSettings() {
        var self = this;
        var processingTypesById = _.indexBy(dtoProcessing.processingTypes, 'id');
        var specimenGroupsById = _.indexBy(dtoProcessing.specimenGroups, 'id');
        var annotTypesById = _.indexBy(dtoProcessing.specimenLinkAnnotationTypes, 'id');

        this.title = 'Specimen Link Types';
        this.header = 'Specimen Link Types are assigned to a processing type, and used to represent a ' +
          'regularly performed processing procedure involving two Specimens: an input, which ' +
          'must be in a specific Specimen Group, and an output, which must be in a specific ' +
          'Specimen Group.';
        this.information = function(spcLinkType) {
          spcLinkTypeModalService.show(spcLinkType, processingTypesById, specimenGroupsById);
        };
        this.add = function(study) {
          $state.go('admin.studies.study.processing.spcLinkTypeAdd');
        };
        this.update = function(spcLinkType) {
          $state.go('admin.studies.study.processing.spcLinkTypeUpdate',
                    { procTypeId: spcLinkType.processingTypeId, spcLinkTypeId: spcLinkType.id });
        };
        this.remove = function(spcLinkType) {
          spcLinkTypeRemoveService.remove(spcLinkType);
        };
        this.panelOpen = studyViewSettings.panelState('spcLinkTypes');
        this.panelToggle = function() {
          studyViewSettings.panelState('spcLinkTypes');
        };

        this.showProcessingType = function (processingTypeId) {
          processingTypeModalService.show(processingTypesById[processingTypeId]);
        };

        this.showSpecimenGroup = function (specimenGroupId) {
          specimenGroupModalService.show(specimenGroupsById[specimenGroupId]);
        };

        var tableData = [];
        dtoProcessing.specimenLinkTypes.forEach(function(slt) {
          var annotTypeNames = [];
          slt.annotationTypeData.forEach(function (annotTypeItem) {
            annotTypeNames.push(annotTypesById[annotTypeItem.annotationTypeId].name);
          });

          tableData.push({
            specimenLinkType: slt,
            processingTypeName: processingTypesById[slt.processingTypeId].name,
            inputGroupName: specimenGroupsById[slt.inputGroupId].name,
            outputGroupName: specimenGroupsById[slt.outputGroupId].name,
            annotTypeNames: annotTypeNames
          });
        });

        $injector.invoke(PanelSettings, this, {
          data: tableData,
          panelStateName: 'spcLinkTypes'
        });
      }

      $scope.panel.spcLinkTypes = new SpcLinkTypesPanelSettings();

    }]);

  return mod;
});
