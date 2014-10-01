/**
 * User service, exposes user model to the rest of the app.
 */
define(['./module'], function(module) {
  'use strict';

  module.factory('adminService', AdminService);

  AdminService.$inject = ['biobankXhrReqService'];

  function AdminService(biobankXhrReqService) {
    var service = {
      aggregateCounts: aggregateCounts
    };
    return service;

    function aggregateCounts() {
      return biobankXhrReqService.call('GET', '/counts');
    }
  }

});
