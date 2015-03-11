/**
 * User service, exposes user model to the rest of the app.
 */
define(['./module'], function(module) {
  'use strict';

  module.factory('adminService', AdminService);

  AdminService.$inject = ['biobankApi'];

  function AdminService(biobankApi) {
    var service = {
      aggregateCounts: aggregateCounts
    };
    return service;

    function aggregateCounts() {
      return biobankApi.get('/counts');
    }
  }

});
