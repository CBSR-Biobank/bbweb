/** Specimen Group helpers */
define(['../module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.service('UserModalService', UserModalService);

  UserModalService.$inject = ['domainEntityModalService', 'addTimeStamps'];

  function UserModalService(domainEntityModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //--

    function show(user) {
      var title = 'User';
      var data = [
        {name: 'Name:',   value: user.name},
        {name: 'Email:',  value: user.email},
        {name: 'Status:', value: user.status}
      ];

      data = data.concat(addTimeStamps.get(user));
      domainEntityModalService.show(title, data);
    }
  }

});
