/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('studies.helpers', []);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('annotTypeModalService', ['modelObjModalService', function (modelObjModalService) {
    this.show = function (annotType) {
      var title = 'Participant Annotation Type';
      var data = [];
      data.push({name: 'Name:', value: annotType.name});
      data.push({name: 'Type:', value: annotType.valueType});

      if (!annotType.required) {
        data.push({name: 'Required:', value: annotType.required ? "Yes" : "No"});
      }

      if (annotType.valueType === 'Select') {
        var optionValues = [];
        for (var name in annotType.options) {
          optionValues.push(annotType.options[name]);
        }

        data.push({
          name: '# Selections Allowed:',
          value: annotType.maxValueCount === 1 ? "Single" : "Multiple"});
        data.push({
          name: 'Selections:',
          value: optionValues.join(", ")});
      }

      data.push({name: 'Description:', value: annotType.description});

      modelObjModalService.show(title, data);
    };
  }]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('specimenGroupModalService', ['modelObjModalService', function (modelObjModalService) {
    this.show = function (specimenGroup) {
      var title = 'Specimen Group';
      var data = [];
      data.push({name: 'Name:', value: specimenGroup.name});
      data.push({name: 'Units:', value: specimenGroup.units});
      data.push({name: 'Anatomical Source:', value: specimenGroup.anatomicalSourceType});
      data.push({name: 'Preservation Type:', value: specimenGroup.preservationType});
      data.push({name: 'Preservation Temperature:', value: specimenGroup.preservationTemperatureType});
      data.push({name: 'Specimen Type:', value: specimenGroup.specimenType});
      data.push({name: 'Description:', value: specimenGroup.description});
      modelObjModalService.show(title, data);
    };
  }]);

  return mod;
});
