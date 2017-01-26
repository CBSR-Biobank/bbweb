/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash'),
      sharedBehaviour = require('../../../../test/EntityPagedListSharedBehaviourSpec');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function () {
      this.element = angular.element('<studies-paged-list></studies-paged-list');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('studiesPagedList');
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

  describe('studiesPagedListComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);

      _.extend(this, SuiteMixin.prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/studiesPagedList/studiesPagedList.html',
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'factory');

    }));

    it('scope is valid on startup', function() {
      this.createCountsSpy(2, 5);
      this.createPagedResultsSpy([]);
      this.createScope();

      expect(this.controller.limit).toBeDefined();
      expect(this.controller.stateData).toBeArrayOfObjects();
      expect(this.controller.stateData).toBeNonEmptyArray();
      expect(this.controller.getItems).toBeFunction();
      expect(this.controller.getItemIcon).toBeFunction();
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
          self.createScope();
        };

        context.getEntitiesLastCallArgs = function () {
          return self.Study.list.calls.mostRecent().args;
        };

      }));

      sharedBehaviour(context);

    });

  });

});
