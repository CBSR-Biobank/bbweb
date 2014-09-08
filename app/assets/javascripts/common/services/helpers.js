/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.helpers', []);

  mod.factory('BbwebRestApi', ['$http', '$q', '$log', function($http, $q, $log) {
    return {
      call: function(method, url, data) {
        var deferred = $q.defer();
        var config = { method: method, url: url };

        if (data) {
          config.data = data;
        }

        $http(config).then(
          function(response) {
            deferred.resolve(response.data.data);
          },
          function(response) {
            $log.error(response.data);
            deferred.reject(response.data);
          }
        );
        return deferred.promise;
      }
    };
  }]);

  /**
   * Hack to reload state and re-initialize the controller.
   */
  mod.service('stateHelper', [
    '$state', '$stateParams',
    function ($state, $stateParams) {
      return {
        reloadAndReinit: function() {
          // could use $state.reload() here but it does not re-initialize the
          // controller
          $state.transitionTo($state.current, $stateParams, {
            reload: true,
            inherit: false,
            notify: true
          });
        }
      };
    }]);

  /**
   * Code originally taken from:
   *
   * http://weblogs.asp.net/dwahlin/building-an-angularjs-modal-service
   *
   */
  mod.service('modalService', ['$modal', function ($modal) {
    var modalDefaults = {
      backdrop: true,
      keyboard: true,
      modalFade: true,
      templateUrl: '/assets/javascripts/common/modal.html'
    };

    var modalOptions = {
      //closeButtonText: 'Close',
      actionButtonText: 'OK',
      headerText: 'Proceed?',
      bodyText: 'Perform this action?'
    };

    this.showModal = function (customModalDefaults, customModalOptions) {
      if (!customModalDefaults) customModalDefaults = {};
      customModalDefaults.backdrop = 'static';
      return this.show(customModalDefaults, customModalOptions);
    };

    this.show = function (customModalDefaults, customModalOptions) {
      //Create temp objects to work with since we're in a singleton service
      var tempModalDefaults = {};
      var tempModalOptions = {};

      //Map angular-ui modal custom defaults to modal defaults defined in service
      angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

      //Map modal.html $scope custom properties to defaults defined in service
      angular.extend(tempModalOptions, modalOptions, customModalOptions);

      if (!tempModalDefaults.controller) {
        tempModalDefaults.controller = function ($scope, $modalInstance) {
          $scope.modalOptions = tempModalOptions;
          $scope.modalOptions.ok = function (result) {
            $modalInstance.close(result);
          };
          $scope.modalOptions.close = function (result) {
            $modalInstance.dismiss('cancel');
          };
        };
      }

      return $modal.open(tempModalDefaults).result;
    };
  }]);


  mod.service('modelObjModalService', ['$modal', 'ngTableParams', function ($modal, ngTableParams) {
    var modalDefaults = {
      backdrop: true,
      keyboard: true,
      modalFade: true,
      templateUrl: '/assets/javascripts/admin/studies/modelObjModal.html'
    };

    this.show = function (title, data) {
      var tempModalDefaults = {};
      angular.extend(tempModalDefaults, modalDefaults);

      tempModalDefaults.controller = ['$scope', '$modalInstance', function ($scope, $modalInstance) {
        $scope.modalOptions = {
          title: title,
          data: data
        };

        /* jshint ignore:start */
        $scope.modalOptions.tableParams = new ngTableParams({
          page:1,
          count: 20
        }, {
          counts: [],                                       // hide page counts control
          total: $scope.modalOptions.data.length,           // length of data
          getData: function($defer, params) {
            $defer.resolve($scope.modalOptions.data);
          }
        });
        /* jshint ignore:end */

        $scope.modalOptions.ok = function (result) {
          $modalInstance.close();
        };
      }];

      return $modal.open(tempModalDefaults).result;
    };

  }]);

  return mod;
});
