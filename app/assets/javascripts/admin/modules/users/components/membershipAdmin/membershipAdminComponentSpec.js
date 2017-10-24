/**
 * Jasmine test suite
 *
 */
/* global angular */

describe('membershipAdminComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test')
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      Object.assign(this, ComponentTestSuiteMixin)

      //this.injectDependencies('')

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<membership-admin></membership-admin>',
          undefined,
          'membershipAdmin')
      }
    })
  })

  it('has valid scope', function() {
    this.createController()
    expect(this.controller.breadcrumbs).toBeDefined()
  })

})
