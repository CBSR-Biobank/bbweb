/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (bindings) {
      var self = this,
          defaultBindings = {},
          actualBindings = {};

      self.nameFilterUpdated = jasmine.createSpy().and.returnValue(null);
      self.stateFilterUpdated = jasmine.createSpy().and.returnValue(null);
      self.filtersCleared = jasmine.createSpy().and.returnValue(null);

      defaultBindings = {
        stateData:            [ 'enabled', 'disbled' ],
        onNameFilterUpdated:  self.nameFilterUpdated,
        onStateFilterUpdated: self.stateFilterUpdated,
        onFiltersCleared:     self.filtersCleared
      };

      _.extend(actualBindings, defaultBindings, bindings);

      self.element = angular.element([
        '<name-and-state-filters ',
        '    state-data="vm.stateData" ',
        '    on-name-filter-updated="vm.onNameFilterUpdated" ',
        '    on-state-filter-updated="vm.onStateFilterUpdated" ',
        '    on-filters-cleared="vm.onFiltersCleared"> ',
        '</name-and-state-filters>'
      ].join(''));
      self.scope = self.$rootScope.$new();
      self.scope.vm = actualBindings;
      self.$compile(self.element)(self.scope);
      self.scope.$digest();
      self.controller = self.element.controller('nameAndStateFilters');
    };

    return SuiteMixin;
  }

  describe('nameAndStateFiltersComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html');
    }));

    it('has valid scope', function() {
      this.createScope();

      expect(this.controller.nameFilter).toBeEmptyString();
      expect(this.controller.selectedState).toBe('all');
      expect(this.controller.stateData[0].id).toBe('all');

      expect(this.controller.nameFilterUpdated).toBeFunction();
      expect(this.controller.stateFilterUpdated).toBeFunction();
      expect(this.controller.clearFilters).toBeFunction();
    });

    it('invokes the callback when the name filter is updated', function() {
      this.createScope();

      this.controller.nameFilter = 'test';
      this.controller.nameFilterUpdated();
      this.scope.$digest();

      expect(this.nameFilterUpdated).toHaveBeenCalled();
    });

    it('invokes the callback when the name state is updated', function() {
      this.createScope();

      this.controller.selectedState = 'enabled';
      this.controller.stateFilterUpdated();
      this.scope.$digest();

      expect(this.stateFilterUpdated).toHaveBeenCalled();
    });

    it('invokes the callback when the filters are cleared', function() {
      this.createScope();

      this.controller.clearFilters();
      this.scope.$digest();

      expect(this.controller.nameFilter).toBeEmptyString();
      expect(this.controller.selectedState).toBe('all');
      expect(this.filtersCleared).toHaveBeenCalled();
    });

  });

});
