/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', ['studies.services']);

  function PanelCtrl($scope, panelTableService, data) {
    this.data = data;
    this.panelOpen = true;
    this.togglePanel = this.togglePanel || function() {
      this.panelOpen = !this.panelOpen;
    };
    this.tableParams = panelTableService.getTableParams(this.data);
  }

  function AnnotTypesPanelCtrl(
    $injector,
    $scope,
    panelTableService,
    annotTypeModalService,
    study,
    annotTypes,
    title,
    header,
    hasRequiredField,
    onAdd,
    onUpdate,
    removeService) {
    this.title = title;
    this.header = header;
    this.hasRequired = hasRequiredField;
    this.information = function(annotType) {
      annotTypeModalService.show(this.title, annotType);
    };
    this.add = onAdd;
    this.update = onUpdate;
    this.remove = function(annotType) {
      removeService.remove(annotType);
    };

    $injector.invoke(PanelCtrl, this, {
      $scope: $scope,
      panelTableService: panelTableService,
      data: annotTypes
    });
  }

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
  mod.controller('StudySummaryTabCtrl', [
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
  mod.controller('ParticipantsTabCtrl', [
    '$injector',
    '$scope',
    '$state',
    'annotTypeModalService',
    'panelTableService',
    'participantAnnotTypeRemoveService',
    'study',
    'annotTypes',
    function (
      $injector,
      $scope,
      $state,
      annotTypeModalService,
      panelTableService,
      participantAnnotTypeRemoveService,
      study,
      annotTypes) {

      $scope.annotTypesPanel = new AnnotTypesPanelCtrl(
        $injector,
        $scope,
        panelTableService,
        annotTypeModalService,
        study,
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
      );
    }]);


  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensTabCtrl', [
    '$injector',
    '$scope',
    '$state',
    'specimenGroupModalService',
    'panelTableService',
    'SpecimenGroupService',
    'specimenGroupRemoveService',
    'study',
    'specimenGroups',
    function(
      $injector,
      $scope,
      $state,
      specimenGroupModalService,
      panelTableService,
      SpecimenGroupService,
      specimenGroupRemoveService,
      study,
      specimenGroups) {

      this.title = 'Specimen Groups';
      this.header = ' A Specimen Group is used to configure a specimen type to be used by the study. ' +
        'It records ownership, summary, storage, and classification information that applies ' +
        'to an entire group or collection of Specimens.';
      this.information = function(specimenGroup) {
        specimenGroupModalService.show(specimenGroup);
      };
      this.add = function(study) {
        $state.go('admin.studies.study.specimens.groupAdd', { studyId: study.id });
      };
      this.update = function(specimenGroup) {
        $state.go('admin.studies.study.specimens.groupUpdate',
                  { studyId: specimenGroup.studyId, specimenGroupId: specimenGroup.id });
      };
      this.remove = function(specimenGroup) {
        specimenGroupRemoveService.remove(specimenGroup);
      };

      $injector.invoke(PanelCtrl, this, {
        $scope: $scope,
        panelTableService: panelTableService,
        data: specimenGroups
      });
      $scope.specimenGroupsPanel = this;
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('CollectionTabCtrl', [
    '$injector',
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
      $injector,
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

      $scope.annotTypesPanel = new AnnotTypesPanelCtrl(
        $injector,
        $scope,
        panelTableService,
        annotTypeModalService,
        study,
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
      );

      function CeventTypesPanelCtrl($injector, $scope, ceventTypes) {
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

        $injector.invoke(PanelCtrl, this, {
          $scope: $scope,
          panelTableService: panelTableService,
          data: ceventTypes
        });
      }

      $scope.ceventTypesPanel = new CeventTypesPanelCtrl($injector, $scope, ceventTypes);

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
  mod.controller('ProcessingTabCtrl', [
    '$injector',
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
      $injector,
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

      $scope.annotTypesPanel = new AnnotTypesPanelCtrl(
        $injector,
        $scope,
        panelTableService,
        annotTypeModalService,
        study,
        annotTypes,
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

      function ProcessingTypesPanelCtrl($injector, $scope, processingTypes) {
        this.title = 'Processing Types';
        this.header = 'A Processing Type describes a regularly performed specimen processing procedure ' +
          'with a unique name (unique to this study). There should be one or more associated ' +
          'Specimen Link Types that (1) further define legal procedures and (2) allow recording ' +
          'of procedures performed on different types of Specimens. ';
        this.information = function(processingType) {
          processingTypeModalService.show(processingType, processingTypes);
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

        $injector.invoke(PanelCtrl, this, {
          $scope: $scope,
          panelTableService: panelTableService,
          data: processingTypes
        });
      }

      $scope.processingTypesPanel = new ProcessingTypesPanelCtrl($injector, $scope, processingTypes);

      function SpcLinkTypesPanelCtrl($injector, $scope, spcLinkTypes) {
        this.title = 'Specimen Link Types';
        this.header = 'Specimen Link Types are assigned to a processing type, and used to represent a ' +
          'regularly performed processing procedure involving two Specimens: an input, which ' +
          'must be in a specific Specimen Group, and an output, which must be in a specific ' +
          'Specimen Group.';
        this.information = function(spcLinkType) {
          spcLinkTypeModalService.show(spcLinkType, spcLinkTypes);
        };
        this.add = function(study) {
          $state.go('admin.studies.study.processing.spcLinkTypeAdd', { studyId: study.id });
        };
        this.update = function(spcLinkType) {
          $state.go('admin.studies.study.processing.spcLinkTypeUpdate',
                    { studyId: spcLinkType.studyId, spcLinkTypeId: spcLinkType.id });
        };
        this.remove = function(spcLinkType) {
          spcLinkTypeRemoveService.remove(spcLinkType);
        };

        $injector.invoke(PanelCtrl, this, {
          $scope: $scope,
          panelTableService: panelTableService,
          data: spcLinkTypes
        });
      }

      $scope.spcLinkTypesPanel = new SpcLinkTypesPanelCtrl($injector, $scope, spcLinkTypes);

    }]);

  return mod;
});
