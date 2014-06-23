/**
 * User controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('study.controllers', ['study.services']);

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   * "user" is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  mod.controller('StudiesCtrl', function($rootScope, $scope, $state, $location, user, studyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];
      $scope.user = user;

      studyService.list().then(function(response) {
        $scope.studies = response.data.sort(studyCompare);
      });

      $scope.addStudy = function() {
        $state.go("admin.studies.edit");
      };

      $scope.studyInformation = function(annotType) {
        $state.go("admin.studies.view", { id: annotType.id });
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
  mod.controller(
    'StudiesTableCtrl',
    function($scope, $rootScope, $filter, $state, $location, $log, ngTableParams, user, studyService) {
      $rootScope.pageTitle = 'Biobank studies';
      $scope.studies = [];

      studyService.list().then(function(response) {
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
        $state.go("admin.studies.edit");
      };

      $scope.studyInformation = function(annotType) {
        $state.go("admin.studies.view", { id: annotType.id });
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
  mod.controller(
    'StudyCtrl',
    function($scope, $rootScope, $stateParams, $state, $location, $log, $filter, user, study) {

      $scope.study = study;
      $scope.description = $scope.study.description;

      // studyService.query($stateParams.id)
      //   .success(function(data) {
      //     $scope.study = data;
      //     $scope.description = $scope.study.description;
      //   })
      //   .error(function() {
      //     $location.path("/studies/error");
      //   });

      $scope.tabSelected = function() {
        /* this event gets picked up by the child controller to update its contents. */
        $scope.$broadcast('tabSelected');
      };
    });

  /**
   * Displays study annotation type summary information in a table. The user can then select a row
   * to display more informaiton for te annotation type.
   */
  mod.controller(
    'participantsPaneCtrl',
    function(
      $scope,
      $stateParams,
      $modal,
      $location,
      $filter,
      $log,
      ngTableParams,
      studyService) {

      var studyId = $stateParams.id;
      $scope.annotationTypes = [];
      $scope.tableParams = getAnnotationTableParams($scope, $filter, ngTableParams);

      /**
       * Creates a modal to display the annotation type details.
       *
       * @param {annotType} the annotation type to display.
       */
      $scope.annotInformation = function(annotType) {
        $modal.open({
          templateUrl: '/assets/javascripts/study/annotationType.html',
          controller: AnnotationTypeCtrl,
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
      $scope.updateAnnotationType = function(annotType) {
        $log.info("editAnnotationType");
        $location.path("/studies/partannot/edit/" + annotType.id);
      };

      /**
       * Switches to the page to remove an annotation type.
       *
       * @param {annotType} the annotation type to be edited.
       */
      $scope.removeAnnotationType = function(annotType) {
        $log.info("removeAnnotationType");
        $location.path("/studies/partannot/remove/" + annotType.id);
      };

      /**
       * This event is received when the user selects the "Participants" tab in the study view page.
       *
       * @param {event} a don't care parameter.
       * @param {args} a don't care parameter.
       */
      $scope.$on('tabSelected', function(event, args) {
        studyService.participantInfo(studyId).then(function(response) {
          $scope.annotationTypes = response.data;

          if ($scope.tableParams.data.length > 0) {
            $scope.tableParams.reload();
          }
        });
      });
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
  mod.controller(
    'StudyEditCtrl',
    function(
      $scope,
      $rootScope,
      $route,
      $routeParams,
      $location,
      $modal,
      $log,
      user,
      studyService) {

      var id = $routeParams.id;

      $rootScope.pageTitle = 'Biobank study';
      if (id === undefined) {
        $scope.title =  "Add new study";
        $scope.study = {
          name: "",
          description: null
        };
      } else {
        $scope.title =  "Update study";
        studyService.query(id).then(function(response) {
          $scope.study = response.data;
        });
      }

      $scope.submit = function(study) {
        var modalInstance = {};

        studyService.addOrUpdate(study)
          .success(function() {
            $location.path('/studies/' + $scope.study.id);
          })
          .error(function(error) {
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
                $route.reload();
              }, function () {
                $location.path('/studies/' + $scope.study.id);
              });
            } else {
              /* some other error */
              modalInstance = $modal.open({
                templateUrl: '/assets/javascripts/common/errorModal.html',
                controller: 'errorModal',
                resolve: {
                  title: function () {
                    return (study.id === undefined) ? "Cannot add study" : "Cannot update study";
                  },
                  message: function() {
                    return "Error: " + error.message;
                  }
                }
              });

              modalInstance.result.then(function(selectedItem) {
                $route.reload();
              }, function () {
                $route.reload();
              });
            }
          });
      };

      $scope.cancel = function(study) {
        if ($scope.study.id === undefined) {
          $location.path('/studies');
        } else {
          $location.path('/studies/' + $scope.study.id);
        }
      };
    });

  /**
   * This controller displays a study annotation type in a modal. The information is displayed
   * in an ng-table.
   */
  var AnnotationTypeCtrl = function ($scope, $modalInstance, ngTableParams, annotType) {
    $scope.annotType = annotType;
    $scope.data = [];

    $scope.data.push({name: 'Name:', value: annotType.name});
    $scope.data.push({name: 'Type:', value: annotType.valueType});

    if (annotType.required !== undefined) {
      $scope.data.push({name: 'Required:', value: annotType.required ? "Yes" : "No"});
    }

    if (annotType.valueType === 'Select') {
      var optionValues = [];
      for (var name in annotType.options) {
        optionValues.push(annotType.options[name]);
      }

      $scope.data.push({
        name: '# Selections Allowed:',
        value: annotType.maxValueCount === 1 ? "Single" : "Multiple"});
      $scope.data.push({
        name: 'Selections:',
        value: optionValues.join(",")});
    }

    $scope.data.push({name: 'Description:', value: annotType.description});

    /* jshint ignore:start */
    $scope.tableParams = new ngTableParams({
      page:1,
      count: 10
    }, {
      counts: [], // hide page counts control
      total: $scope.data.length,           // length of data
      getData: function($defer, params) {
        $defer.resolve($scope.data);
      }
    });
    /* jshint ignore:end */
  };

  mod.controller(
    'StudyAnnotationTypeEditCtrl',
    function ($scope, $log, $routeParams) {
      $log.info("StudyAnnotationTypeEditCtrl:", $routeParams);
    });

  mod.controller('StudyAnnotationTypeRemoveCtrl',
    function ($scope, $log, $routeParams) {
      $log.info("StudyAnnotationTypeRemoveCtrl:", $routeParams);
    });

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
