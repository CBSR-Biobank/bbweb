/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('membershipAdminComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('$q',
                              'Membership',
                              'Factory')

      this.createController = () => {
        this.Membership.list =
          jasmine.createSpy().and.returnValue(this.$q.when(this.Factory.pagedResult([])));

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
