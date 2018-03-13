/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  function nameFiltersharedBehaviour(context) {

    describe('Name filter shared behaviour', function() {

      beforeEach(function() {
        this.nameFilterUpdated = jasmine.createSpy().and.returnValue(null);
      });

      it('valid scope for name filter', function() {
        context.createController();
        expect(this.controller.nameFilter).toBeEmptyString();
        expect(this.controller.nameFilterUpdated).toBeFunction();
        expect(this.controller.clearFilters).toBeFunction();
      });


      it('invokes the callback when the name filter is updated', function() {
        context.createController();
        this.controller.nameFilter = 'test';
        this.controller.nameFilterUpdated();
        this.scope.$digest();
        expect(this.nameFilterUpdated).toHaveBeenCalled();
      });

      it('invokes the callback when the filters are cleared', function() {
        context.createController();
        this.controller.clearFilters();
        this.scope.$digest();
        expect(this.controller.nameFilter).toBeEmptyString();
        expect(this.filtersCleared).toHaveBeenCalled();
      });

    });
  }

  function stateFiltersharedBehaviour(context) {

    describe('Name filter shared behaviour', function() {

      it('has valid scope for state filter', function() {
        context.createController();
        expect(this.controller.selectedState).toBe('all');
        expect(this.controller.stateFilterUpdated).toBeFunction();
        expect(this.controller.clearFilters).toBeFunction();
      });

      it('invokes the callback when the state is updated', function() {
        context.createController();
        this.controller.selectedState = 'enabled';
        this.controller.stateFilterUpdated();
        this.scope.$digest();
        expect(this.stateFilterUpdated).toHaveBeenCalled();
      });

      it('invokes the callback when the filters are cleared', function() {
        context.createController();
        this.controller.clearFilters();
        this.scope.$digest();
        expect(this.controller.selectedState).toBe('all');
        expect(this.filtersCleared).toHaveBeenCalled();
      });

    });

  }

  return {
    nameFiltersharedBehaviour: nameFiltersharedBehaviour,
    stateFiltersharedBehaviour: stateFiltersharedBehaviour
  };
});
