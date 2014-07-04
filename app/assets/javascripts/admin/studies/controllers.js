/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', ['studies.services']);

  // For debugging
  //
  // mod.run(['$rootScope', '$state', '$stateParams',
  //          function ($rootScope, $state, $stateParams) {
  //            $rootScope.$state = $state;
  //            $rootScope.$stateParams = $stateParams;
  //          }]);

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesCtrl', [
    '$rootScope', '$scope', '$state', '$log', 'user', 'StudyService',
    function($rootScope, $scope, $state, $log, user, StudyService) {
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
    '$scope', '$rootScope', '$stateParams', '$state', '$log', '$filter', 'user', 'study',
    function($scope, $rootScope, $stateParams, $state, $log, $filter, user, study) {

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
   * Displays study participant information in a table.
   */
  mod.controller('ParticipantsPaneCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    'annotTypeModalService',
    'panelTableService',
    'participantAnnotTypeRemoveService',
    'annotTypes',
    function(
      $scope,
      $state,
      $stateParams,
      annotTypeModalService,
      panelTableService,
      participantAnnotTypeRemoveService,
      annotTypes) {

      var studyId = $stateParams.studyId;
      $scope.panel = {};
      $scope.panel.title = 'Participant Annotation Types';
      $scope.panel.header = 'Participant annotations allow a study to collect custom named and ' +
        'defined pieces of data for each participant. Annotations are optional and are not ' +
        'required to be defined.';
      $scope.panel.annotTypes = annotTypes;
      $scope.panel.tableParams = panelTableService.getTableParams($scope.panel.annotTypes);

      if ($scope.panel.tableParams.data.length > 0) {
        $scope.panel.tableParams.reload();
      }

      $scope.panel.annotTypeInformation = function(annotType) {
        annotTypeModalService.show(annotType);
      };

      $scope.panel.addAnnotType = function(study) {
        $state.go('admin.studies.study.participantAnnotTypeAdd', { studyId: studyId });
      };

      $scope.panel.updateAnnotType = function(annotType) {
        $state.go('admin.studies.study.participantAnnotTypeUpdate',
                  { studyId: annotType.studyId, annotTypeId: annotType.id });
      };

      $scope.panel.removeAnnotType = function(annotType) {
        participantAnnotTypeRemoveService.remove($state, $stateParams, annotType);
      };
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensPaneCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    'specimenGroupModalService',
    'panelTableService',
    'SpecimenGroupService',
    'specimenGroupRemoveService',
    'specimenGroups',
    function(
      $scope,
      $state,
      $stateParams,
      specimenGroupModalService,
      panelTableService,
      SpecimenGroupService,
      specimenGroupRemoveService,
      specimenGroups) {

      var studyId = $stateParams.studyId;
      $scope.specimenGroups = specimenGroups;
      $scope.tableParams = panelTableService.getTableParams($scope.specimenGroups);

      if ($scope.tableParams.data.length > 0) {
        $scope.tableParams.reload();
      }

      $scope.specimenGroupInformation = function(specimenGroup) {
        specimenGroupModalService.show(specimenGroup);
      };

      $scope.addSpecimenGroup = function(study) {
        $state.go('admin.studies.study.specimenGroupAdd', { studyId: studyId });
      };

      $scope.updateSpecimenGroup = function(specimenGroup) {
        $state.go('admin.studies.study.specimenGroupUpdate',
                  { studyId: specimenGroup.studyId, specimenGroupId: specimenGroup.id });
      };

      $scope.removeSpecimenGroup = function(specimenGroup) {
        specimenGroupRemoveService.remove($state, $stateParams, specimenGroup);
      };
    }]);

  /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('CollectionPaneCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    'ceventTypeModalService',
    'ceventAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelTableService',
    'ceventTypes',
    'annotTypes',
    'specimenGroups',
    function(
      $scope,
      $state,
      $stateParams,
      ceventTypeModalService,
      ceventAnnotTypeRemoveService,
      annotTypeModalService,
      panelTableService,
      ceventTypes,
      annotTypes,
      specimenGroups) {

      var studyId = $stateParams.studyId;
      $scope.panel = {};
      $scope.panel.title = 'Collection Event Annotation Types';
      $scope.panel.header = 'Collection event annotations allow a study to collect custom named and ' +
        'defined pieces of data for each collection event. Annotations are optional and are not ' +
        'required to be defined.';
      $scope.panel.ceventTypes = ceventTypes;
      $scope.panel.annotTypes = annotTypes;
      $scope.panel.specimenGroups = specimenGroups;
      $scope.panel.tableParams = panelTableService.getTableParams($scope.panel.ceventTypes);

      if ($scope.panel.tableParams.data.length > 0) {
        $scope.panel.tableParams.reload();
      }

      $scope.panel.ceventTypeInformation = function(ceventType) {
        ceventTypeModalService.show(ceventType);
      };

      $scope.panel.addCeventType = function(study) {
        $state.go('admin.studies.study.ceventTypeAdd', { studyId: studyId });
      };

      $scope.panel.updateCeventType = function(ceventType) {
        $state.go('admin.studies.study.ceventTypeUpdate',
                  { studyId: ceventType.studyId, ceventTypeId: ceventType.id });
      };

      $scope.panel.removeCeventType = function(ceventType) {
        studyCeventTypeRemove(
          $scope, $state, $stateParams, modalService, CollectionEventTypeService, ceventType);
      };

      $scope.panel.annotTypeInformation = function(ceventType) {
        ceventTypeModalService.show(ceventType);
      };

      $scope.panel.addAnnotType = function(study) {
        $state.go('admin.studies.study.ceventAnnotTypeAdd', { studyId: studyId });
      };

      $scope.panel.updateAnnotType = function(ceventType) {
        $state.go('admin.studies.study.ceventAnnotTypeUpdate',
                  { studyId: ceventType.studyId, ceventTypeId: ceventType.id });
      };

      $scope.panel.removeAnnotType = function(ceventType) {
        ceventAnnotTypeRemoveService.remove($state, $stateParams, annotType);
      };
    }]);

  /**
   * Called to add a study.
   */
  mod.controller('StudyAddCtrl', [
    '$scope', '$state', '$stateParams', '$location', 'modalService', 'user', 'study', 'StudyService',
    function($scope, $state, $stateParams, $location, modalService, user, study, StudyService) {

      $scope.title =  "Add new study";
      $scope.study = study;

      $scope.submit = function(study) {
        StudyService.addOrUpdate(study)
          .success(function() {
            $state.go('admin.studies');
          })
          .error(function(error) {
            studySaveError(
              $scope, modalService, study, error,
              // on OK
              function() {
                // could use $state.reload() here but it does not re-initialize the
                // controller
                $state.transitionTo($state.current, $stateParams, {
                  reload: true,
                  inherit: false,
                  notify: true
                });
              },
              // on Cancel
              function() {
                $state.go('admin.studies');
              }
            );
          });
      };

      $scope.cancel = function(study) {
        $state.go('admin.studies');
      };
    }]);

  /**
   * Called to update the summary information for study.
   */
  mod.controller('StudyUpdateCtrl', [
    '$scope', '$state', '$stateParams', '$location', 'modalService', 'user', 'study', 'StudyService',
    function($scope, $state, $stateParams, $location, modalService, user, study, StudyService) {

      $scope.title = "Update study";
      $scope.study = study;

      $scope.submit = function(study) {
        StudyService.addOrUpdate(study)
          .success(function() {
            $state.go('admin.studies.study', { studyId: $scope.study.id });
          })
          .error(function(error) {
            studySaveError(
              $scope, modalService, study, error,
              function() {
                // could use $state.reload() here but it does not re-initialize the
                // controller
                $state.transitionTo($state.current, $stateParams, {
                  reload: true,
                  inherit: false,
                  notify: true
                });
              },
              function() {
                $state.go('admin.studies.study.summary');
              }
            );
          });
      };

      $scope.cancel = function(study) {
        $state.go('admin.studies.study.summarypane', { studyId: $stateParams.studyId });
      };
    }]);

  /**
   * Called where there was an error when attempting to add or update a study.
   */
  function studySaveError($scope, modalService, study, error, onOk, onCancel) {
    var modalOptions = {
      closeButtonText: 'Cancel',
      actionButtonText: 'OK'
    };

    if (error.message.indexOf("expected version doesn't match current version") > -1) {
      /* concurrent change error */
      modalOptions.headerText = 'Modified by another user';
      modalOptions.bodyText = 'Another user already made changes to this study. Press OK to make ' +
        ' your changes again, or Cancel to dismiss your changes.';
    } else {
      /* some other error */
      modalOptions.headerText = study.id ?  'Cannot update study' : 'Cannot add study';
      modalOptions.bodyText = 'Error: ' + error.message;
    }

    modalService.showModal({}, modalOptions).then(function (result) {
      onOk();
    }, function () {
      onCancel();
    });
  }

  return mod;
});
