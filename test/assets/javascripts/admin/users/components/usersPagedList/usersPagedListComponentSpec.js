/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks           = require('angularMocks'),
      _               = require('lodash'),
      sharedBehaviour = require('../../../../test/EntityPagedListSharedBehaviourSpec');

  describe('usersPagedListComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<users-paged-list></users-paged-list',
          undefined,
          'usersPagedList');
      };

      SuiteMixin.prototype.createCountsSpy = function (disabled, enabled, retired) {
        var counts = {
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };

        spyOn(this.UserCounts, 'get').and.returnValue(this.$q.when(counts));
      };

      SuiteMixin.prototype.createUserListSpy = function (users) {
        var reply = this.factory.pagedResult(users);
        spyOn(this.User, 'list').and.returnValue(this.$q.when(reply));
      };

      SuiteMixin.prototype.createEntity = function () {
        var entity = new this.User(this.factory.user());
        return entity;
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/users/components/usersPagedList/usersPagedList.html',
        '/assets/javascripts/common/components/nameEmailStateFilters/nameEmailStateFilters.html',
        '/assets/javascripts/common/components/debouncedTextInput/debouncedTextInput.html',
        '/assets/javascripts/common/components/stateAndTimestamps/stateAndTimestamps.html',
        '/assets/javascripts/common/components/entityTimestamps/entityTimestamps.html');

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

    }));

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

    it('on startup, state changed to login page if user is not authorized', function() {
      this.UserCounts.get = jasmine.createSpy()
        .and.returnValue(this.$q.reject({ status: 401, message: 'unauthorized'}));
      spyOn(this.$state, 'go').and.returnValue(null);
      this.createController();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
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

});
