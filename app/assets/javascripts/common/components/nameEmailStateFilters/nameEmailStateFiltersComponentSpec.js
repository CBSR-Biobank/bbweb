/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash'),
      filtersSharedBehaviour = require('../../../test/filtersSharedBehaviour');

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
        self.emailFilterUpdated = jasmine.createSpy().and.returnValue(null);
        self.stateFilterUpdated = jasmine.createSpy().and.returnValue(null);
        self.filtersCleared = jasmine.createSpy().and.returnValue(null);

        defaultBindings = {
          stateData:            [ 'enabled', 'disbled' ],
          selectedState:        'all',
          onNameFilterUpdated:  self.nameFilterUpdated,
          onEmailFilterUpdated: self.emailFilterUpdated,
          onStateFilterUpdated: self.stateFilterUpdated,
          onFiltersCleared:     self.filtersCleared
        };

        _.extend(actualBindings, defaultBindings, bindings);

        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<name-email-state-filters ',
            '    state-data="vm.stateData" ',
            '    selected-state="' + actualBindings.selectedState + '" ',
            '    on-name-filter-updated="vm.onNameFilterUpdated" ',
            '    on-email-filter-updated="vm.onEmailFilterUpdated" ',
            '    on-state-filter-updated="vm.onStateFilterUpdated" ',
            '    on-filters-cleared="vm.onFiltersCleared"> ',
            '</name-email-state-filters>'
          ].join(''),
          actualBindings,
          'nameEmailStateFilters');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/nameEmailStateFilters/nameEmailStateFilters.html',
        '/assets/javascripts/common/components/debouncedTextInput/debouncedTextInput.html');
    }));

    describe('for name filter', function() {
      var context = {};

      beforeEach(function() {
        context.createController = this.createController.bind(this);
      });

      filtersSharedBehaviour.nameFiltersharedBehaviour(context);

    });

    describe('for state filter', function() {
      var context = {};

      beforeEach(function() {
        context.createController = this.createController.bind(this);
      });

      filtersSharedBehaviour.stateFiltersharedBehaviour(context);

    });

    it('has valid scope', function() {
      this.createController();
      expect(this.controller.emailFilter).toBeEmptyString();
      expect(this.controller.emailFilterUpdated).toBeFunction();
    });

    it('invokes the callback when the email filter is updated', function() {
      this.createController();

      this.controller.emailFilter = 'test';
      this.controller.emailFilterUpdated();
      this.scope.$digest();

      expect(this.emailFilterUpdated).toHaveBeenCalled();
    });

    it('invokes the callback when the filters are cleared', function() {
      this.createController();
      this.controller.clearFilters();
      this.scope.$digest();
      expect(this.controller.emailFilter).toBeEmptyString();
      expect(this.filtersCleared).toHaveBeenCalled();
    });

  });

});
