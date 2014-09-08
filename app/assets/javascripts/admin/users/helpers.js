/** Specimen Group helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.users.helpers', []);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('UserModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (user) {
        var title = 'Specimen Group';
        var data = [];

        data.push({name: 'Name:',   value: user.name});
        data.push({name: 'Email:',  value: user.email});
        data.push({name: 'Status:', value: user.status});

        data = data.concat(addTimeStampsService.get(user));
        modelObjModalService.show(title, data);
      };
    }]);

  return mod;
});
