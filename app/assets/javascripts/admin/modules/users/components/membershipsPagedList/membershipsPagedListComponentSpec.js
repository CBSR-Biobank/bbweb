/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash'
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/EntityPagedListSharedBehaviourSpec';

describe('membershipsPagedListComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q',
                              'Membership',
                              'NameFilter',
                              '$state',
                              'Factory');

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<memberships-paged-list></memberships-paged-list',
          undefined,
          'membershipsPagedList');

      this.createEntity = () =>
        this.Membership.create(this.Factory.membership());

      this.createMembershipListSpy = (members) => {
        var reply = this.Factory.pagedResult(members);
        this.Membership.list = jasmine.createSpy().and.returnValue(this.$q.when(reply));
      }
    })
  })

  it('scope is valid on startup', function() {
    const members = [ this.Factory.membership() ];
    this.createMembershipListSpy(members)
    this.createController();

    expect(this.controller.limit).toBeDefined();
    expect(this.controller.getItems).toBeFunction();
    expect(this.controller.getItemIcon).toBeFunction();
  })

  describe('users', function () {

    var context = {};

    beforeEach(function () {
      context.createController = () => {
        const members = _.range(2).map(() => this.Factory.membership());
        this.createMembershipListSpy(members)
        this.createController();
      };

      context.getEntitiesLastCallArgs = () => this.Membership.list.calls.mostRecent().args;
      context.validFilters = [ 'nameFilter']
      context.sortFieldIds = ['name' ];
      context.defaultSortFiled = 'name';
    });

    sharedBehaviour(context);

  });

})
