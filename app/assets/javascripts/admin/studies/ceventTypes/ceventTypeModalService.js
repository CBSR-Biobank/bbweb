define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.service('ceventTypeModalService', ceventTypeModalService);

  ceventTypeModalService.$inject = ['$filter', 'modelObjModalService', 'addTimeStamps'];

  /**
   * Displays a collection event type in a modal. The information is displayed in an ng-table.
   */
  function ceventTypeModalService($filter, modelObjModalService, addTimeStamps) {
    var service = {
      show: show
    };
    return service;

    //-------
    function show (ceventType, specimenGroups, annotTypes) {
      var title = 'Collection Event Type';
      var specimenGroupsById = _.indexBy(specimenGroups, 'id');
      var annotTypesById = _.indexBy(annotTypes, 'id');

      var sgDataStrings = [];
      ceventType.specimenGroupData.forEach(function (sgItem) {
        var specimenGroup = specimenGroupsById[sgItem.specimenGroupId];
        if (!specimenGroup) {
          throw new Error('specimen group not found');
        }
        sgDataStrings.push(specimenGroup.name + ' (' + sgItem.maxCount + ', ' + sgItem.amount +
                           ' ' + specimenGroup.units + ')');
      });

      var atDataStrings = [];
      ceventType.annotationTypeData.forEach(function (atItem) {
        var annotType = annotTypesById[atItem.annotationTypeId];
        if (!annotType) {
          throw new Error('annotation type not found');
        }
        atDataStrings.push(annotType.name + (atItem.required ? ' (Req)' : ' (N/R)'));
      });

      var data = [];
      data.push({name: 'Name:', value: ceventType.name});
      data.push({name: 'Recurring:', value: ceventType.recurring ? 'Yes' : 'No'});
      data.push({name: 'Specimen Groups (Count, Amount):', value: sgDataStrings.join(', ')});
      data.push({name: 'Annotation Types:', value: atDataStrings.join(', ')});
      data.push({name: 'Description:', value: ceventType.description});
      data = data.concat(addTimeStamps.get(ceventType));
      modelObjModalService.show(title, data);
    }

  }

});
