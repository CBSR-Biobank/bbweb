/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.centres.helpers', []);

  /**
   * Called where there was an error on attempting to add or update a centre.
   */
  mod.service('centreEditService', [
    '$state', 'modalService', 'stateHelper', 'CentreService',
    function ($state, modalService, stateHelper, CentreService) {

      var onError = function (centre, error, onCancel) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this centre. Press OK to make ' +
            ' your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText = 'Cannot ' + (centre.id ?  'update' : 'add') + ' centre';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(
          function () {
            stateHelper.reloadAndReinit();
          }, function () {
            onCancel();
          });
      };

      return {
        edit : function($scope, onSubmitSuccess, onSubmitErrorCancel, onCancel) {
          $scope.submit = function(centre) {
            CentreService.addOrUpdate(centre).then(
              onSubmitSuccess,
              function(error) {
                onError($scope.centre, error, onSubmitErrorCancel);
              });
          };

          $scope.cancel = function() {
            onCancel();
          };
        }
      };
    }
  ]);

  return mod;
});
