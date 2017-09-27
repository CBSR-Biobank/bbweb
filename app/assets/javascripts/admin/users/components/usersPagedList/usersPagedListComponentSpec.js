/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../../test/EntityPagedListSharedBehaviourSpec';

describe('usersPagedListComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'User',
                              'UserCounts',
                              'UserState',
                              'NameFilter',
                              'EmailFilter',
                              'StateFilter',
                              '$state',
                              'factory');

      this.createController = () =>
        ComponentTestSuiteMixin.prototype.createController.call(
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
        var reply = this.factory.pagedResult(users);
        spyOn(this.User, 'list').and.returnValue(this.$q.when(reply));
      };

      this.createEntity = () => this.User.create(this.factory.user());
    });
  });

  it('scope is valid on startup', function() {
    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();

    expect(this.controller.limit).toBeDefined();
    expect(this.controller.filters[this.StateFilter.name].allChoices()).toBeArrayOfObjects();
    expect(this.controller.filters[this.StateFilter.name].allChoices()).toBeNonEmptyArray();
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

    beforeEach(inject(function () {
      var self = this;

      context.createController = function (usersCount) {
        var users;
        usersCount = usersCount || 0;
        users = _.map(_.range(usersCount), self.createEntity.bind(self));
        self.createCountsSpy(2, 5, 3);
        self.createUserListSpy(users);
        self.createController();
      };

      context.getEntitiesLastCallArgs = function () {
        return self.User.list.calls.mostRecent().args;
      };

      context.stateFilterValue = this.UserState.ACTIVE;
      context.sortFields = ['Name', 'Email', 'State'];
      context.defaultSortFiled = 'name';
    }));

    sharedBehaviour(context);

  });

  describe('getItemIcon', function() {

    beforeEach(function() {
      this.createCountsSpy(2, 5);
      this.createUserListSpy([]);
      this.createController();
    });

    it('getItemIcon returns a valid icon', function() {
      var self = this,
          statesInfo = [
            { state: this.UserState.REGISTERED, icon: 'glyphicon-cog' },
            { state: this.UserState.ACTIVE,     icon: 'glyphicon-user' },
            { state: this.UserState.LOCKED,     icon: 'glyphicon-lock' }
          ];

      statesInfo.forEach(function (info) {
        var user = new self.User(self.factory.user({ state: info.state }));
        expect(self.controller.getItemIcon(user)).toEqual(info.icon);
      });
    });

    it('getItemIcon throws an error for and invalid state', function() {
      var self = this,
          user = new this.User(this.factory.user({ state: this.factory.stringNext() }));

      expect(function () {
        self.controller.getItemIcon(user);
      }).toThrowError(/invalid user state/);
    });

  });

  it('updates items when email filter is updated', function() {
    var emailFilterValue = 'test@test.com',
        spyArgs;

    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();
    this.controller.updateSearchFilter('EmailFilter')(emailFilterValue);
    this.scope.$digest();

    spyArgs = this.User.list.calls.mostRecent().args[0];
    expect(spyArgs).toBeObject();
    expect(spyArgs.filter).toEqual('email:like:' + emailFilterValue);
  });

  it('filters are cleared', function() {
    this.createCountsSpy(2, 5);
    this.createUserListSpy([]);
    this.createController();
    this.controller.updateSearchFilter('EmailFilter')('test@test.com');
    this.controller.filtersCleared();
    this.scope.$digest();
    expect(this.controller.emailFilter).toBeEmptyString();
  });

});
