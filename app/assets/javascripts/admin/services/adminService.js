/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  adminService.$inject = ['biobankApi'];

  function adminService(biobankApi) {
    var service = {
      aggregateCounts: aggregateCounts
    };
    return service;

    // FIXME: move this to the domain layer?
    function aggregateCounts() {
      return biobankApi.get('/counts');
    }
  }

  return adminService;
});
