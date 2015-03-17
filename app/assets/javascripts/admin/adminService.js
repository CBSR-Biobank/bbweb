/**
 * User service, exposes user model to the rest of the app.
 */
define([], function() {
  'use strict';

  adminService.$inject = ['biobankApi'];

  function adminService(biobankApi) {
    var service = {
      aggregateCounts: aggregateCounts
    };
    return service;

    function aggregateCounts() {
      return biobankApi.get('/counts');
    }
  }

  return adminService;
});
