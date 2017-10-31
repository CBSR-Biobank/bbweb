/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function AdminService(UrlService, biobankApi) {
  var service = {
    aggregateCounts
  };
  return service;

  // FIXME: move this to the domain layer?
  function aggregateCounts() {
    return biobankApi.get(UrlService.url('dtos/counts'));
  }
}

export default ngModule => ngModule.service('adminService', AdminService)
