/** Specimen Group helpers */
define(['../../module'], function(module) {
  'use strict';

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  module.service('specimenGroupModalService', specimenGroupModalService);

  specimenGroupModalService.$inject = [
    'modelObjModalService', 'addTimeStamps'
  ];

  function specimenGroupModalService(modelObjModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //--

    function show(specimenGroup) {
      var title = 'Specimen Group';
      var data = [
        {name: 'Name:',                     value: specimenGroup.name},
        {name: 'Units:',                    value: specimenGroup.units},
        {name: 'Anatomical Source:',        value: specimenGroup.anatomicalSourceType},
        {name: 'Preservation Type:',        value: specimenGroup.preservationType},
        {name: 'Preservation Temperature:', value: specimenGroup.preservationTemperatureType},
        {name: 'Specimen Type:',            value: specimenGroup.specimenType},
        {name: 'Description:',              value: specimenGroup.description}
      ];
      data = data.concat(addTimeStamps.get(specimenGroup));
      modelObjModalService.show(title, data);
    }
  }

});
