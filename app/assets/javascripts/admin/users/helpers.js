/** Specimen Group helpers */
define(['../module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.service('UserModalService', [
    'modelObjModalService', 'addTimeStamps',

    function (modelObjModalService, addTimeStamps) {
      this.show = function (user) {
        var title = 'User';
        var data = [];

        data.push({name: 'Name:',   value: user.name});
        data.push({name: 'Email:',  value: user.email});
        data.push({name: 'Status:', value: user.status});

        data = data.concat(addTimeStamps.get(user));
        modelObjModalService.show(title, data);
      };
    }
  ]);

});
