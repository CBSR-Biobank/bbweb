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
        $scope.studies = response.data.sort(studyCompare);
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
        $state.go("admin.studies.panels");
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
    '$modal',
    'modalService',
    'annotTypeModalService',
    '$filter',
    '$log',
    'ngTableParams',
    'ParticipantAnnotTypeService',
    'annotTypes',
    function(
      $scope,
      $state,
      $stateParams,
      $modal,
      modalService,
      annotTypeModalService,
      $filter,
      $log,
      ngTableParams,
      ParticipantAnnotTypeService,
      annotTypes) {

      var studyId = $stateParams.studyId;
      $scope.annotationTypes = annotTypes;
      $scope.tableParams =
        getAnnotationTableParams($scope, $filter, ngTableParams, $scope.annotationTypes);

      if ($scope.tableParams.data.length > 0) {
        $scope.tableParams.reload();
      }

      /**
       * Creates a modal to display the annotation type details.
       *
       * @param {annotType} the annotation type to display.
       */
      $scope.annotInformation = function(annotType) {
        annotTypeModalService.show(annotType);
      };

      /**
       * Switches to the page to edit an annotation type.
       *
       * @param {annotType} the annotation type to be edited.
       */
      $scope.addAnnotationType = function(study) {
        $state.go('admin.studies.study.participantAnnotTypeAdd', { studyId: studyId });
      };

      /**
       * Switches to the page to edit an annotation type.
       *
       * @param {annotType} the annotation type to be edited.
       */
      $scope.updateAnnotationType = function(annotType) {
        $state.go('admin.studies.study.participantAnnotTypeUpdate',
                  { studyId: annotType.studyId, annotTypeId: annotType.id });
      };

      /**
       * Switches to the page to remove an annotation type.
       *
       * @param {annotType} the annotation type to be edited.
       */
      $scope.removeAnnotationType = function(annotType) {
        studyAnnotationTypeRemove(
          $scope, $state, $stateParams, modalService, ParticipantAnnotTypeService, annotType);
      };
    }]);

    /**
   * Displays study specimen configuration information in a table.
   */
  mod.controller('SpecimensPaneCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    '$modal',
    'modalService',
    'specimenGroupModalService',
    '$filter',
    'ngTableParams',
    'SpecimenGroupService',
    'specimenGroups',
    function(
      $scope,
      $state,
      $stateParams,
      $modal,
      modalService,
      specimenGroupModalService,
      $filter,
      ngTableParams,
      SpecimenGroupService,
      specimenGroups) {

      var studyId = $stateParams.studyId;
      $scope.specimenGroups = specimenGroups;
      $scope.tableParams =
        getSpecimenGroupTableParams($scope, $filter, ngTableParams, $scope.specimenGroups);

      if ($scope.tableParams.data.length > 0) {
        $scope.tableParams.reload();
      }

      /**
       * Creates a modal to display the annotation type details.
       *
       * @param {specimenGroup} the annotation type to display.
       */
      $scope.specimenGroupInformation = function(specimenGroup) {
        specimenGroupModalService.show(specimenGroup);
      };

      /**
       * Switches to the page to edit an annotation type.
       *
       * @param {specimenGroup} the annotation type to be edited.
       */
      $scope.addSpecimenGroup = function(study) {
        $state.go('admin.studies.study.specimenGroupAdd', { studyId: studyId });
      };

      /**
       * Switches to the page to edit an annotation type.
       *
       * @param {specimenGroup} the annotation type to be edited.
       */
      $scope.updateSpecimenGroup = function(specimenGroup) {
        $state.go('admin.studies.study.specimenGroupUpdate',
                  { studyId: specimenGroup.studyId, specimenGroupId: specimenGroup.id });
      };

      /**
       * Switches to the page to remove an annotation type.
       *
       * @param {specimenGroup} the annotation type to be edited.
       */
      $scope.removeSpecimenGroup = function(specimenGroup) {
        studySpecimenGroupRemove(
          $scope, $state, $stateParams, modalService, SpecimenGroupService, specimenGroup);
      };
    }]);

  /**
   * Returns an ng-table with containing the data passed in the parameter.
   */
  var getAnnotationTableParams = function($scope, $filter, ngTableParams, annotationTypes) {
    /* jshint ignore:start */
    return new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        name: 'asc'       // initial sorting
      }
    },{
      counts: [], // hide page counts control
      total: function() { return annotationTypes.length; },
      getData: function($defer, params) {
        var data = annotationTypes;
        params.total(annotationTypes.length);
        var orderedData = params.sorting()
          ? $filter('orderBy')(data, params.orderBy())
          : data;
        $defer.resolve(orderedData.slice(
          (params.page() - 1) * params.count(),
          params.page() * params.count()));
      }
    });
    /* jshint ignore:end */
  };

  /**
   * Returns an ng-table with containing the data passed in the parameter.
   */
  var getSpecimenGroupTableParams = function($scope, $filter, ngTableParams, specimenGroups) {
    /* jshint ignore:start */
    return new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        name: 'asc'       // initial sorting
      }
    },{
      counts: [], // hide page counts control
      total: function() { return $scope.specimenGroups.length; },
      getData: function($defer, params) {
        var data = specimenGroups;
        params.total(specimenGroups.length);
        var orderedData = params.sorting()
          ? $filter('orderBy')(data, params.orderBy())
          : data;
        $defer.resolve(orderedData.slice(
          (params.page() - 1) * params.count(),
          params.page() * params.count()));
      }
    });
    /* jshint ignore:end */
  };

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
            $state.go('admin.studies.panels');
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
                $state.go('admin.studies.panels');
              }
            );
          });
      };

      $scope.cancel = function(study) {
        $state.go('admin.studies.panels');
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

  /**
   * Used to sort a list of studies. Returns the comparison of names of the two studies.
   */
  var studyCompare = function(a, b) {
    if (a.name < b.name) {
      return -1;
    } else if (a.name > b.name) {
      return 1;
    }
    return 0;
  };

  function studyAnnotationTypeRemove(
    $scope,
    $state,
    $stateParams,
    modalService,
    ParticipantAnnotTypeService,
    annotType) {

    var modalOptions = {
      closeButtonText: 'Cancel',
      headerText: 'Remove Participant Annotation Type',
      bodyText: 'Are you sure you want to remove annotation type ' + annotType.name + '?'
    };

    modalService.showModal({}, modalOptions).then(function (result) {
      ParticipantAnnotTypeService.remove(annotType)
        .success(function() {
          // could use $state.reload() here but it does not re-initialize the
          // controller
          $state.transitionTo($state.current, $stateParams, {
            reload: true,
            inherit: false,
            notify: true
          });
        })
        .error(function(error) {
          studyAnnotationTypeRemoveError($state, modalService, annotType, error);
        });
    }, function() {
      $state.go('admin.studies.study.participants');
    });
  }

  /*
   * Called when an annotation type cannot be removed.
   */
  function studyAnnotationTypeRemoveError($state, modalService, annotType, error) {
    var modalOptions = {
      closeButtonText: 'Cancel',
      headerText: 'Remove failed',
      bodyText: 'Annotation type ' + annotType.name + ' cannot be removed: ' + error.message
    };

    modalService.showModal({}, modalOptions).then(function (result) {
      $state.go('admin.studies.study.participants');
    }, function () {
      $state.go('admin.studies.study.participants');
    });
  }

  return mod;
});
