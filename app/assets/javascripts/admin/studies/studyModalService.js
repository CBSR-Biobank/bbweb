/** Specimen Group helpers */
define(['../module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.service('studyModalService', studyModalService);

  studyModalService.$inject = [
    '$filter', 'domainEntityModalService', 'addTimeStamps',
  ];

  function studyModalService($filter, domainEntityModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //--

    function show(study) {
      var title = 'Specimen Group';
      var description = study.description || '';
      var data = [
        {name: 'Name:',        value: study.name},
        {name: 'Description:', value: $filter('truncate')(description, 60)},
        {name: 'Status:',      value: study.status}
      ];
      data = data.concat(addTimeStamps.get(study));
      domainEntityModalService.show(title, data);
    }
  }

});
