define(['../module'], function(module) {
  'use strict';

  module.service('modelObjService', modelObjService);

  //modelObjService.$inject = [];

  /**
   * Utilities for services that access domain objects.
   */
  function modelObjService() {
    var service = {
      setDescription: setDescription
    };
    return service;

    /**
     * Does not set the description field in a command if it is null or length 0.
     */
    function setDescription(cmd, description) {
      if (description && (description.length > 0)) {
        cmd.description = description;
      }
    }
  }



});
