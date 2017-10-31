/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../../index'

describe('membershipAdminComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
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
