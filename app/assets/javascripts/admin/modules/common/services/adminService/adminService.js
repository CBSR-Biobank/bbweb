/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS service that provides common functionality.
 *
 * @memberOf admin.common.services
 */
class AdminService {

  constructor(biobankApi, resourceErrorService) {
    'ngInject';
    Object.assign(this, { biobankApi, resourceErrorService });
  }

  /**
   * Retrieves the counts of the different types of aggregate root entities in the system.
   *
   * If the request fails, the promise is handled by {@link
   * ng.base.services.ResourceErrorService#checkUnauthorized ResourceErrorService.checkUnauthorized()}.
   *
   * @return {Promise<admin.common.services.AdminService.AggregateCounts>}
   */
  // FIXME: move this to the domain layer?
  aggregateCounts() {
    return this.biobankApi.get(this.biobankApi.url('dtos/counts'))
      .catch(this.resourceErrorService.checkUnauthorized());
  }
}

/**
 * @typedef admin.common.services.AdminService.AggregateCounts
 * @type object
 *
 * @property {int} studies - the number of studies in the system
 *
 * @property {int} centres - the number of centres in the system
 *
 * @property {int} users - the number of users in the system
 */


export default ngModule => ngModule.service('adminService', AdminService)
