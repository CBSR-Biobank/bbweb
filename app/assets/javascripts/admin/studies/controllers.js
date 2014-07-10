/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', ['studies.services']);

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesCtrl', [
    '$rootScope', '$scope', '$state', '$log', 'user', 'StudyService', 'studyCompareService',
    function($rootScope, $scope, $state, $log, user, StudyService, studyCompareService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];
      $scope.user = user;

      StudyService.list().then(function(response) {
        $scope.studies = response.data.sort(studyCompareService.compare);
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
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesTableCtrl', [
    '$scope', '$rootScope', '$filter', '$state', '$log', 'ngTableParams',
    'user', 'StudyService',
    function($scope, $rootScope, $filter, $state, $log, ngTableParams, user, StudyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      StudyService.list().then(function(response) {
        $scope.studies = response.data;

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
   *
   * See http://stackoverflow.com/questions/22881782/angularjs-tabset-does-not-show-the-correct-state-when-the-page-is-reloaded
   */
  mod.controller('StudySummaryCtrl', [
    '$scope', '$rootScope', '$state', '$log', '$filter', 'user', 'study',
    function($scope, $rootScope, $state, $log, $filter, user, study) {

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
        $state.go('admin.studies.study', { studyId: $scope.study.id });
      };

      studyEditService.edit($scope, callback, callback, callback);
    }]);

  /**
   * Displays study participant information in a table.
   */
  mod.controller('ParticipantsPaneCtrl', [
    '$scope',
    '$state',
    'annotTypeModalService',
    'panelTableService',
    'participantAnnotTypeRemoveService',
    'study',
    'annotTypes',
    function(
      $scope,
      $state,
      annotTypeModalService,
      panelTableService,
      participantAnnotTypeRemoveService,
      study,
      annotTypes) {

      $scope.panel = {
        annotTypes: {
          data: annotTypes,
          title: 'Participant Annotation Types',
          header: 'Participant annotations allow a study to collect custom named and ' +
            'defined pieces of data for each participant. Annotations are optional and are not ' +
            'required to be defined.',
          tableParams: panelTableService.getTableParams(annotTypes),
          hasRequired: true,
          information: function(annotType) {
            annotTypeModalService.show('Participant Annotation Type', annotType);
          },
          add: function(study) {
            $state.go('admin.studies.study.participants.annotTypeAdd', { studyId: study.id });
          },
          update: function(annotType) {
            $state.go('admin.studies.study.participants.annotTypeUpdate',
                      { studyId: annotType.studyId, annotTypeId: annotType.id });
          },
          remove: function(annotType) {
            participantAnnotTypeRemoveService.remove(annotType);
          }
        }
      };
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensPaneCtrl', [
    '$scope',
    '$state',
    'specimenGroupModalService',
    'panelTableService',
    'SpecimenGroupService',
    'specimenGroupRemoveService',
    'study',
    'specimenGroups',
    function(
      $scope,
      $state,
      specimenGroupModalService,
      panelTableService,
      SpecimenGroupService,
      specimenGroupRemoveService,
      study,
      specimenGroups) {

      $scope.panel = {
        specimenGroups: {
          data: specimenGroups,
          title: 'Specimen Groups',
          header: ' A Specimen Group is used to configure a specimen type to be used by the study. ' +
            'It records ownership, summary, storage, and classification information that applies ' +
            'to an entire group or collection of Specimens.',
          tableParams: panelTableService.getTableParams(specimenGroups),
          information: function(specimenGroup) {
            specimenGroupModalService.show(specimenGroup);
          },
          add: function(study) {
            $state.go('admin.studies.study.specimens.groupAdd', { studyId: study.id });
          },
          update: function(specimenGroup) {
            $state.go('admin.studies.study.specimens.groupUpdate',
                      { studyId: specimenGroup.studyId, specimenGroupId: specimenGroup.id });
          },
          remove: function(specimenGroup) {
            specimenGroupRemoveService.remove(specimenGroup);
          }
        }
      };
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('CollectionPaneCtrl', [
    '$scope',
    '$state',
    'ceventTypeModalService',
    'ceventTypeRemoveService',
    'ceventAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelTableService',
    'study',
    'ceventTypes',
    'annotTypes',
    'specimenGroups',
    function(
      $scope,
      $state,
      ceventTypeModalService,
      ceventTypeRemoveService,
      ceventAnnotTypeRemoveService,
      annotTypeModalService,
      panelTableService,
      study,
      ceventTypes,
      annotTypes,
      specimenGroups) {

      $scope.panel = {
        annotTypes: {
          title: 'Collection Event Annotation Types',
          header: 'Collection event annotations allow a study to collect custom named and defined ' +
            'pieces of data for each collection event. Annotations are optional and are not ' +
            'required to be defined.',
          data: annotTypes,
          tableParams: panelTableService.getTableParams(annotTypes),
          hasRequired: false,
          information: function(annotType) {
            annotTypeModalService.show('Collection Event Annotation Type', annotType);
          },
          add: function(study) {
            $state.go('admin.studies.study.collection.ceventAnnotTypeAdd', { studyId: study.id });
          },
          update: function(annotType) {
            $state.go('admin.studies.study.collection.ceventAnnotTypeUpdate',
                      { studyId: annotType.studyId, annotTypeId: annotType.id });
          },
          remove: function(annotType) {
            ceventAnnotTypeRemoveService.remove(annotType);
          }
        },
        ceventTypes: {
          title: 'Collection Event Types',
          header: 'A Collection Event Type defines a classification name, unique to the Study, to a ' +
            'participant visit. A participant visit is a record of when specimens were collected ' +
            'from a participant at a collection centre.',
          data: ceventTypes,
          tableParams: panelTableService.getTableParams(ceventTypes),
          information: function(ceventType) {
            ceventTypeModalService.show(ceventType, specimenGroups, annotTypes);
          },
          add: function(study) {
            $state.go('admin.studies.study.collection.ceventTypeAdd', { studyId: study.id });
          },
          update: function(ceventType) {
            $state.go('admin.studies.study.collection.ceventTypeUpdate',
                      { studyId: ceventType.studyId, ceventTypeId: ceventType.id });
          },
          remove: function(ceventType) {
            ceventTypeRemoveService.remove(ceventType);
          }
        }
      };

      $scope.cetSgNames = {};
      ceventTypes.forEach(function (cet) {
        $scope.cetSgNames[cet.id] = [];
        cet.specimenGroupData.forEach(function (sgItem) {
          $scope.cetSgNames[cet.id].push(sgItem.name);
        });
      });
    }]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('ProcessingPaneCtrl', [
    '$scope',
    '$state',
    'processingTypeModalService',
    'processingTypeRemoveService',
    'spcLinkAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelTableService',
    'study',
    'processingTypes',
    'annotTypes',
    'spcLinkTypes',
    function(
      $scope,
      $state,
      processingTypeModalService,
      processingTypeRemoveService,
      spcLinkAnnotTypeRemoveService,
      annotTypeModalService,
      panelTableService,
      study,
      processingTypes,
      annotTypes,
      spcLinkTypes) {

      $scope.panel = {
        processingTypes: {
          title: 'Processing Types',
          header: 'A Processing Type describes a regularly performed specimen processing procedure ' +
            'with a unique name (unique to this study). There should be one or more associated ' +
            'Specimen Link Types that (1) further define legal procedures and (2) allow recording ' +
            'of procedures performed on different types of Specimens. ',
          data: processingTypes,
          tableParams: panelTableService.getTableParams(processingTypes),
          information: function(processingType) {
            processingTypeModalService.show(processingType, processingTypes);
          },
          add: function(study) {
            $state.go('admin.studies.study.processing.processingTypeAdd', { studyId: study.id });
          },
          update: function(processingType) {
            $state.go('admin.studies.study.processing.processingTypeUpdate',
                      { studyId: processingType.studyId, processingTypeId: processingType.id });
          },
          remove: function(processingType) {
            processingTypeRemoveService.remove(processingType);
          }
        },
        annotTypes: {
          title: 'Specimen Link Annotation Types',
          header: 'Specimen link annotations allow a study to collect custom named and defined ' +
            'pieces of data when processing specimens. Annotations are optional and are not ' +
            'required to be defined.',
          data: annotTypes,
          tableParams: panelTableService.getTableParams(annotTypes),
          hasRequired: false,
          information: function(annotType) {
            annotTypeModalService.show('Processing Event Annotation Type', annotType);
          },
          add: function(study) {
            $state.go('admin.studies.study.processing.spcLinkAnnotTypeAdd', { studyId: study.id });
          },
          update: function(annotType) {
            $state.go('admin.studies.study.processing.spcLinkAnnotTypeUpdate',
                      { studyId: annotType.studyId, annotTypeId: annotType.id });
          },
          remove: function(annotType) {
            spcLinkAnnotTypeRemoveService.remove(annotType);
          }
        },
        spcLinkTypes: {
          title: 'Specimen Link Types',
          header: 'Specimen Link Types are assigned to a processing type, and used to represent a ' +
            'regularly performed processing procedure involving two Specimens: an input, which ' +
            'must be in a specific Specimen Group, and an output, which must be in a specific ' +
            'Specimen Group. ',
          data: spcLinkTypes,
          tableParams: panelTableService.getTableParams(spcLinkTypes),
          information: function(spcLinkType) {
            spcLinkTypeModalService.show(spcLinkType, spcLinkTypes);
          },
          add: function(study) {
            $state.go('admin.studies.study.processing.spcLinkTypeAdd', { studyId: study.id });
          },
          update: function(spcLinkType) {
            $state.go('admin.studies.study.processing.spcLinkTypeUpdate',
                      { studyId: spcLinkType.studyId, spcLinkTypeId: spcLinkType.id });
          },
          remove: function(spcLinkType) {
            spcLinkTypeRemoveService.remove(spcLinkType);
          }
        }
      };
    }]);

  return mod;
});
