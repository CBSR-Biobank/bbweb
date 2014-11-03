define(['../../module'], function(module) {
  'use strict';

  module.service('annotTypeModalService', annotTypeModalService);

  annotTypeModalService.$inject = ['domainEntityModalService', 'addTimeStamps'];

  /**
   *
   */
  function annotTypeModalService(domainEntityModalService, addTimeStamps) {
    var service = {
      show: show
    };

    return service;

    //-------

    function show(title, annotType) {
      var data = [];
      data.push({name: 'Name:', value: annotType.name});
      data.push({name: 'Type:', value: annotType.valueType});

      if (typeof annotType.required !== 'undefined') {
        data.push({name: 'Required:', value: annotType.required ? 'Yes' : 'No'});
      }

      if (annotType.valueType === 'Select') {
        if (!annotType.options || annotType.options.length < 1) {
          throw new Error('invalid annotation type options');
        }

        data.push(
          {
            name: '# Selections Allowed:',
            value: annotType.maxValueCount === 1 ? 'Single' : 'Multiple'
          },
          {
            name: 'Selections:',
            value: annotType.options.join(', ')
          });
      }

      data.push({name: 'Description:', value: annotType.description});
      data = data.concat(addTimeStamps.get(annotType));

      domainEntityModalService.show(title, data);
    }

  }

});
