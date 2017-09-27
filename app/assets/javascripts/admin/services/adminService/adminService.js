/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  adminService.$inject = ['UrlService', 'biobankApi'];

  function adminService(UrlService, biobankApi) {
    var service = {
      aggregateCounts: aggregateCounts
    };
    return service;

    // FIXME: move this to the domain layer?
    function aggregateCounts() {
      return biobankApi.get(UrlService.url('dtos/counts'));
    }
  }

  return adminService;
});
