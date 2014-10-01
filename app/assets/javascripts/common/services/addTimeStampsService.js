define(['../module'], function(module) {
  'use strict';

  module.service('addTimeStamps', addTimeStamps);

  addTimeStamps.$inject = ['$filter'];

  /**
   * All domain objects have 'timeAdded' and 'timeModified' fields. This service will return these fields
   * in an array so that they can be displayed in a table (see modelObjModalService).
   */
  function addTimeStamps($filter) {
    var service = {
      get: get
    };
    return service;

    //-------

    function get(modelObj) {
      var data = [];
      data.push({name: 'Added:', value: $filter('timeago')(modelObj.timeAdded)});
      if (modelObj.timeModified !== null) {
        data.push({name: 'Last updated:', value: $filter('timeago')(modelObj.timeModified)});
      }
      return data;
    }
  }

});
