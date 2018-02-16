/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../../../test/behaviours/EntityPagedListSharedBehaviourSpec';

describe('usersPagedListComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q',
                              'User',
                              'UserCounts',
                              'UserState',
                              'NameFilter',
                              'EmailFilter',
                              'StateFilter',
                              'resourceErrorService',
                              '$state',
                              'Factory');

      this.createController = (userCounts = {}) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<users-paged-list user-counts="vm.userCounts"></users-paged-list>',
          { userCounts },
          'usersPagedList');

      this.createCounts = (registered = 0, active = 0, locked = 0) => {
        const total = registered + active + locked;
        return { total, registered, active, locked };
      };

      this.createCountsSpy = (counts) => {
        spyOn(this.UserCounts, 'get').and.returnValue(this.$q.when(counts));
      };

      this.createUserListSpy = (users) => {
        var reply = this.Factory.pagedResult(users);
        spyOn(this.User, 'list').and.returnValue(this.$q.when(reply));
      };

      this.createEntity = () => this.User.create(this.Factory.user());
    });
  });

  it('scope is valid on startup', function() {
    const counts = this.createCounts(2, 5);
    this.createCountsSpy(counts)
    this.createUserListSpy([]);
    this.createController(counts);

    expect(this.controller.limit).toBeDefined();
    expect(this.controller.filters.stateFilter.allChoices()).toBeArrayOfObjects();
    expect(this.controller.filters.stateFilter.allChoices()).toBeNonEmptyArray();
    expect(this.controller.getItems).toBeFunction();
    expect(this.controller.getItemIcon).toBeFunction();
  });

  it('when user counts fails', function() {
    const counts = this.createCounts(2, 5);
    const errFunc = jasmine.createSpy().and.returnValue(null);
    this.resourceErrorService.checkUnauthorized = jasmine.createSpy().and.returnValue(errFunc);
    this.UserCounts.get = jasmine.createSpy()
      .and.returnValue(this.$q.reject({ status: 400, message: 'testing'}));
    this.createController(counts);
    expect(errFunc).toHaveBeenCalled();
  });

  describe('users', function () {

    var context = {};

    beforeEach(function () {
      context.createController = (usersCount = 0) => {
        const counts = this.createCounts(2, 5);
        this.createCountsSpy(counts)
        const users = _.range(usersCount).map(() => this.createEntity());
        this.createUserListSpy(users);
        this.createController(counts);
      };

      context.getEntitiesLastCallArgs = () => this.User.list.calls.mostRecent().args;

      context.stateFilterValue = this.UserState.ACTIVE;
      context.validFilters = [ 'nameFilter', 'stateFilter', 'emailFilter' ]
      context.sortFieldIds = ['name', 'email', 'state'];
      context.defaultSortFiled = 'name';
    });

    sharedBehaviour(context);

  });

  describe('getItemIcon', function() {

    beforeEach(function() {
      const counts = this.createCounts(2, 5);
      this.createCountsSpy(counts)
      this.createUserListSpy([]);
      this.createController(counts);
    });

    it('getItemIcon returns a valid icon', function() {
      const statesInfo = [
            { state: this.UserState.REGISTERED, icon: 'glyphicon-cog' },
            { state: this.UserState.ACTIVE,     icon: 'glyphicon-user' },
            { state: this.UserState.LOCKED,     icon: 'glyphicon-lock' }
          ];

      statesInfo.forEach(info => {
        const user = this.User.create(this.Factory.user({ state: info.state }));
        expect(this.controller.getItemIcon(user)).toEqual(info.icon);
      });
    });

    it('getItemIcon throws an error for and invalid state', function() {
      var user = new this.User(this.Factory.user({ state: this.Factory.stringNext() }));

      expect(() => {
        this.controller.getItemIcon(user);
      }).toThrowError(/invalid user state/);
    });

  });

  it('updates items when email filter is updated', function() {
    var emailFilterValue = 'test@test.com',
        spyArgs;

    const counts = this.createCounts(2, 5);
    this.createCountsSpy(counts)
    this.createUserListSpy([]);
    this.createController(counts);
    this.controller.updateSearchFilter('emailFilter')(emailFilterValue);
    this.scope.$digest();

    spyArgs = this.User.list.calls.mostRecent().args[0];
    expect(spyArgs).toBeObject();
    expect(spyArgs.filter).toEqual('email:like:' + emailFilterValue);
  });

  it('filters are cleared', function() {
    const counts = this.createCounts(2, 5);
    this.createCountsSpy(counts)
    this.createUserListSpy([]);
    this.createController(counts);
    this.controller.updateSearchFilter('emailFilter')('test@test.com');
    this.controller.filtersCleared();
    this.scope.$digest();
    expect(this.controller.emailFilter).toBeEmptyString();
  });

});
