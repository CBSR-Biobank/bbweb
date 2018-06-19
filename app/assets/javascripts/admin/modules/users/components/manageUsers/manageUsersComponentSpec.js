/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import faker from 'faker'
import ngModule from '../../index'

describe('manageUsersComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('$q',
                              'User',
                              'UserCounts',
                              'Factory')

      this.createController = (counts) => {
        this.User.list =
          jasmine.createSpy().and.returnValue(this.$q.when(this.Factory.pagedResult([])));
        this.UserCounts.get = jasmine.createSpy().and.returnValue(this.$q.when(counts))


        this.createControllerInternal(
          '<manage-users></manage-users>',
          undefined,
          'manageUsers')
      }

      this.createUserCounts =
        (registered = faker.random.number(),
         active     = faker.random.number(),
         locked     = faker.random.number()) => new this.UserCounts({
           total:      registered + active + locked,
           registered: registered,
           active:     active,
           locked:     locked
         })
    })
  })

  it('has valid scope', function() {
    const counts = this.createUserCounts();
    this.createController(counts)
    expect(this.controller.breadcrumbs).toBeDefined()
    expect(this.controller.haveUsers).toBe(counts.total > 0)
  })

  it('contains users paged list', function() {
    const counts = this.createUserCounts();
    this.createController(counts)
    expect(this.element.find('users-paged-list').length).toBe(1)
  })

})
