define(['../../module'], function(module) {
  'use strict';

  module.service('processingTypeModalService', processingTypeModalService);

  processingTypeModalService.$inject = ['$filter', 'domainEntityModalService', 'addTimeStamps'];

  /**
   * Displays a processing type in a modal. The information is displayed in an ng-table.
   */
  function processingTypeModalService($filter, domainEntityModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //-------
    function show (processingType) {
      var title = 'Processing Type';
      var data = [];
      data.push({name: 'Name:', value: processingType.name});
      data.push({name: 'Enabled:', value: processingType.enabled ? 'Yes' : 'No'});
      data.push({name: 'Description:', value: processingType.description});
      data = data.concat(addTimeStamps.get(processingType));
      domainEntityModalService.show(title, data);
    }
  }

});
