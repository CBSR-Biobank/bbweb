/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../../../test/behaviours/EntityPagedListSharedBehaviourSpec';

describe('usersPagedListComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q',
                              'User',
                              'UserCounts',
                              'UserState',
                              'NameFilter',
                              'EmailFilter',
                              'StateFilter',
                              '$state',
                              'Factory');

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<users-paged-list></users-paged-list',
          undefined,
          'usersPagedList');

      this.createCountsSpy = (disabled, enabled, retired) => {
        var counts = {
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };

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
    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();

    expect(this.controller.limit).toBeDefined();
    expect(this.controller.filters.stateFilter.allChoices()).toBeArrayOfObjects();
    expect(this.controller.filters.stateFilter.allChoices()).toBeNonEmptyArray();
    expect(this.controller.getItems).toBeFunction();
    expect(this.controller.getItemIcon).toBeFunction();
  });

  it('when user counts fails', function() {
    this.UserCounts.get = jasmine.createSpy()
      .and.returnValue(this.$q.reject({ status: 400, message: 'testing'}));
    this.createController();
    expect(this.controller.counts).toEqual({});
  });

  describe('users', function () {

    var context = {};

    beforeEach(function () {
      context.createController = (usersCount) => {
        usersCount = usersCount || 0;
        const users = _.range(usersCount).map(() => this.createEntity());
        this.createCountsSpy(2, 5, 3);
        this.createUserListSpy(users);
        this.createController();
      };

      context.getEntitiesLastCallArgs = () => this.User.list.calls.mostRecent().args;

      context.stateFilterValue = this.UserState.ACTIVE;
      context.sortFieldIds = ['name', 'email', 'state'];
      context.defaultSortFiled = 'name';
    });

    sharedBehaviour(context);

  });

  describe('getItemIcon', function() {

    beforeEach(function() {
      this.createCountsSpy(2, 5);
      this.createUserListSpy([]);
      this.createController();
    });

    it('getItemIcon returns a valid icon', function() {
      var statesInfo = [
            { state: this.UserState.REGISTERED, icon: 'glyphicon-cog' },
            { state: this.UserState.ACTIVE,     icon: 'glyphicon-user' },
            { state: this.UserState.LOCKED,     icon: 'glyphicon-lock' }
          ];

      statesInfo.forEach((info) => {
        var user = this.User.create(this.Factory.user({ state: info.state }));
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

    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();
    this.controller.updateSearchFilter('emailFilter')(emailFilterValue);
    this.scope.$digest();

    spyArgs = this.User.list.calls.mostRecent().args[0];
    expect(spyArgs).toBeObject();
    expect(spyArgs.filter).toEqual('email:like:' + emailFilterValue);
  });

  it('filters are cleared', function() {
    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();
    this.controller.updateSearchFilter('emailFilter')('test@test.com');
    this.controller.filtersCleared();
    this.scope.$digest();
    expect(this.controller.emailFilter).toBeEmptyString();
  });

});
