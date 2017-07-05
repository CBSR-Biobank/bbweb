/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('nameAndStateFiltersComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (bindings) {
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

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<name-and-state-filters ',
            '    state-data="vm.stateData" ',
            '    on-name-filter-updated="vm.onNameFilterUpdated" ',
            '    on-state-filter-updated="vm.onStateFilterUpdated" ',
            '    on-filters-cleared="vm.onFiltersCleared"> ',
            '</name-and-state-filters>'
          ].join(''),
          actualBindings,
          'nameAndStateFilters');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html');
    }));

    it('has valid scope', function() {
      this.createController();

      expect(this.controller.nameFilter).toBeEmptyString();
      expect(this.controller.selectedState).toBe('all');
      expect(this.controller.stateData[0].id).toBe('all');

      expect(this.controller.nameFilterUpdated).toBeFunction();
      expect(this.controller.stateFilterUpdated).toBeFunction();
      expect(this.controller.clearFilters).toBeFunction();
    });

    it('invokes the callback when the name filter is updated', function() {
      this.createController();

      this.controller.nameFilter = 'test';
      this.controller.nameFilterUpdated();
      this.scope.$digest();

      expect(this.nameFilterUpdated).toHaveBeenCalled();
    });

    it('invokes the callback when the name state is updated', function() {
      this.createController();

      this.controller.selectedState = 'enabled';
      this.controller.stateFilterUpdated();
      this.scope.$digest();

      expect(this.stateFilterUpdated).toHaveBeenCalled();
    });

    it('invokes the callback when the filters are cleared', function() {
      this.createController();

      this.controller.clearFilters();
      this.scope.$digest();

      expect(this.controller.nameFilter).toBeEmptyString();
      expect(this.controller.selectedState).toBe('all');
      expect(this.filtersCleared).toHaveBeenCalled();
    });

  });

});
