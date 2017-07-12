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

  describe('Component: studiesPagedList', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<studies-paged-list></studies-paged-list',
          undefined,
          'studiesPagedList');
      };

      SuiteMixin.prototype.createCountsSpy = function (disabled, enabled, retired) {
        var counts = {
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };

        spyOn(this.StudyCounts, 'get').and.returnValue(this.$q.when(counts));
      };

      SuiteMixin.prototype.createPagedResultsSpy = function (studies) {
        var reply = this.factory.pagedResult(studies);
        spyOn(this.Study, 'list').and.returnValue(this.$q.when(reply));
      };

      SuiteMixin.prototype.createEntity = function () {
        var entity = new this.Study(this.factory.study());
        return entity;
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studiesPagedList/studiesPagedList.html',
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'StudyState',
                              '$state',
                              'factory');

    }));

    it('scope is valid on startup', function() {
      this.createCountsSpy(2, 5);
      this.createPagedResultsSpy([]);
      this.createController();

      expect(this.controller.limit).toBeDefined();
      expect(this.controller.stateData).toBeArrayOfObjects();
      expect(this.controller.stateData).toBeNonEmptyArray();
      expect(this.controller.getItems).toBeFunction();
      expect(this.controller.getItemIcon).toBeFunction();
    });

    it('on startup, state changed to login page if user is not authorized', function() {
      this.StudyCounts.get = jasmine.createSpy()
        .and.returnValue(this.$q.reject({ status: 401, message: 'unauthorized'}));
      spyOn(this.$state, 'go').and.returnValue(null);
      this.createController();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
    });

    it('when study counts fails', function() {
      this.StudyCounts.get = jasmine.createSpy()
        .and.returnValue(this.$q.reject({ status: 400, message: 'testing'}));
      this.createController();
      expect(this.controller.counts).toEqual({});
    });

    describe('studies', function () {

      var context = {};

      beforeEach(inject(function () {
        var self = this;

        context.createController = function (studiesCount) {
          var studies;
          studiesCount = studiesCount || 0;
          studies = _.map(_.range(studiesCount), self.createEntity.bind(self));
          self.createCountsSpy(2, 5, 3);
          self.createPagedResultsSpy(studies);
          self.createController();
        };

        context.getEntitiesLastCallArgs = function () {
          return self.Study.list.calls.mostRecent().args;
        };

      }));

      sharedBehaviour(context);

    });

    describe('getItemIcon', function() {

      beforeEach(function() {
        this.createCountsSpy(2, 5);
        this.createPagedResultsSpy([]);
        this.createController();
      });

      it('getItemIcon returns a valid icon', function() {
        var self = this,
            statesInfo = [
              { state: this.StudyState.DISABLED, icon: 'glyphicon-cog' },
              { state: this.StudyState.ENABLED,  icon: 'glyphicon-ok-circle' },
              { state: this.StudyState.RETIRED,  icon: 'glyphicon-remove-sign' }
            ];

        statesInfo.forEach(function (info) {
          var study = new self.Study(self.factory.study({ state: info.state }));
          expect(self.controller.getItemIcon(study)).toEqual(info.icon);
        });
      });

      it('getItemIcon throws an error for and invalid state', function() {
        var self = this,
            study = new this.Study(this.factory.study({ state: this.factory.stringNext() }));

        expect(function () {
          self.controller.getItemIcon(study);
        }).toThrowError(/invalid study state/);
      });

    });

  });

});
