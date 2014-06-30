/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.controllers', ['study.services']);

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
  mod.controller('StudiesCtrl', function($rootScope, $scope, $state, $location, user, StudyService) {
    $rootScope.pageTitle = 'Biobank studies';
    $scope.studies = [];
    $scope.user = user;

    StudyService.list().then(function(response) {
      $scope.studies = response.data.sort(studyCompare);
    });

    $scope.addStudy = function() {
      $state.go("admin.studies.add");
    };

    $scope.studyInformation = function(study) {
      $state.go("admin.studies.study.summary", { studyId: study.id });
    };

    $scope.tableView = function() {
      $state.go("admin.studies.table");
    };
  });

  /**
   * Displays a list of studies in an ng-table.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesTableCtrl', function(
    $scope,
    $rootScope,
    $filter,
    $state,
    $location,
    $log,
    ngTableParams,
    user,
    StudyService) {
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

  });

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   *
   * See http://stackoverflow.com/questions/22881782/angularjs-tabset-does-not-show-the-correct-state-when-the-page-is-reloaded
   */
  mod.controller('StudySummaryCtrl', function(
    $scope,
    $rootScope,
    $stateParams,
    $state,
    $log,
    $filter,
    user,
    study) {

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

  });

  /**
   * Displays study annotation type summary information in a table. The user can then select a row
   * to display more informaiton for te annotation type.
   */
  mod.controller('ParticipantsPaneCtrl', function(
    $scope,
    $state,
    $stateParams,
    $modal,
    $location,
    $filter,
    $log,
    ngTableParams,
    ParticipantAnnotTypeService) {

    var studyId = $stateParams.studyId;
    $scope.annotationTypes = [];
    $scope.tableParams = getAnnotationTableParams($scope, $filter, ngTableParams);

    /**
     * Creates a modal to display the annotation type details.
     *
     * @param {annotType} the annotation type to display.
     */
    $scope.annotInformation = function(annotType) {
      $modal.open({
        templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeModal.html',
        controller: 'AnnotationTypeModalCtrl',
        resolve: {
          annotType: function () {
            return annotType;
          }
        }
      });
    };

    /**
     * Switches to the page to edit an annotation type.
     *
     * @param {annotType} the annotation type to be edited.
     */
    $scope.addAnnotationType = function(study) {
      $log.info("addAnnotationType");
      $state.go('admin.studies.study.participantAnnotTypeAdd',
                { studyId: studyId });
    };

    /**
     * Switches to the page to edit an annotation type.
     *
     * @param {annotType} the annotation type to be edited.
     */
    $scope.updateAnnotationType = function(annotType) {
      $log.info("updateAnnotationType");
      $state.go('admin.studies.study.participantAnnotTypeUpdate',
                { studyId: annotType.studyId, annotTypeId: annotType.id });
    };

    /**
     * Switches to the page to remove an annotation type.
     *
     * @param {annotType} the annotation type to be edited.
     */
    $scope.removeAnnotationType = function(annotType) {
      $log.info("removeAnnotationType");
      $state.go('admin.studies.study.participantAnnotTypeRemove',
                { studyId: annotType.studyId, annotTypeId: annotType.id });
    };

    $scope.tabSelected = function() {
      ParticipantAnnotTypeService.getAll(studyId).then(function(response) {
        $scope.annotationTypes = response.data;

        if ($scope.tableParams.data.length > 0) {
          $scope.tableParams.reload();
        }
      });
    };
  });

  /**
   * Returns an ng-table with containing the data passed in the parameter.
   */
  var getAnnotationTableParams = function($scope, $filter, ngTableParams) {
    /* jshint ignore:start */
    return new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        name: 'asc'       // initial sorting
      }
    },{
      counts: [], // hide page counts control
      total: function() { return $scope.annotationTypes.length; },
      getData: function($defer, params) {
        var data = $scope.annotationTypes;
        params.total($scope.annotationTypes.length);
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
   * Called to add or update the summary information for study.
   */
  mod.controller('StudyEditCtrl', function(
    $scope,
    $state,
    $stateParams,
    $location,
    $modal,
    $log,
    user,
    study,
    StudyService) {

    if ($state.current.name === "admin.studies.add")  {
      $scope.title =  "Add new study";
      $scope.study = {
        name: "",
        description: null
      };
    } else {
      $scope.title = "Update study";
      $scope.study = study;
    }

    $scope.submit = function(study) {
      StudyService.addOrUpdate(study)
        .success(function() {
          $state.go('admin.studies.study', { id: $scope.study.id });
        })
        .error(function(error) {
          studySaveError($scope, $state, $modal, study, error);
        });
    };

    $scope.cancel = function(study) {
      if ($state.current.name === "admin.studies.add")  {
        $state.go('admin.studies.panels');
      } else if ($stateParams.studyId) {
        $state.go('admin.studies.study.summarypane', { studyId: $stateParams.studyId });
      } else {
        throw new Error("state params studyId is null");
      }
    };
  });

  var studySaveError = function($scope, $state, $modal, study, error) {
    var modalInstance = {};

    if (error.message.indexOf("expected version doesn't match current version") > -1) {
      /* concurrent change error */
      modalInstance = $modal.open({
        templateUrl: '/assets/javascripts/common/errorModal.html',
        controller: 'errorModal',
        resolve: {
          title: function () {
            return "Modified by another user";
          },
          message: function() {
            return "Another user already made changes to this study. Press OK to make " +
              " your changes again, or Cancel to dismiss your changes.";
          }
        }
      });

      modalInstance.result.then(function(selectedItem) {
        $state.$reload();
      }, function () {
        $state.go('admin.studies');
      });
    } else {
      /* some other error */
      modalInstance = $modal.open({
        templateUrl: '/assets/javascripts/common/errorModal.html',
        controller: 'errorModal',
        resolve: {
          title: function () {
            return study.id ?  "Cannot update study" : "Cannot add study";
          },
          message: function() {
            return "Error: " + error.message;
          }
        }
      });

      modalInstance.result.then(function(selectedItem) {
        $state.reload();
      }, function () {
        $state.reload();
      });
    }
  };

  var studyCompare = function(a, b) {
    if (a.name < b.name) {
      return -1;
    } else if (a.name > b.name) {
      return 1;
    }
    return 0;
  };

  return mod;
});
