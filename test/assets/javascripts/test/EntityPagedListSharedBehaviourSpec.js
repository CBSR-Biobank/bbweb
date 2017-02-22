/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  function sharedBehaviour(context) {

    describe('EntityPagedListSharedBehaviour', function() {

      beforeEach(inject(function(testUtils) {
        testUtils.addCustomMatchers();
      }));

      it('has valid scope', function() {
        context.createController();

        expect(this.controller.nameFilter).toBeEmptyString();
        expect(this.controller.sortFields).toContainAll(['Name', 'State']);
        expect(this.controller.selectedState).toBe('all');
        expect(this.controller.pagerOptions.limit).toBe(5);
        expect(this.controller.pagerOptions.sort).toBe('name');

        expect(this.controller.nameFilterUpdated).toBeFunction();
        expect(this.controller.stateFilterUpdated).toBeFunction();
        expect(this.controller.pageChanged).toBeFunction();
        expect(this.controller.sortFieldSelected).toBeFunction();
        expect(this.controller.filtersCleared).toBeFunction();
      });

      it('updates items when name filter is updated', function() {
        var nameFilterValue = 'test',
            spyArgs;

        context.createController(0);

        this.controller.nameFilterUpdated(nameFilterValue);
        this.scope.$digest();

        spyArgs = context.getEntitiesLastCallArgs()[0];
        expect(spyArgs).toBeObject();
        expect(spyArgs.filter).toEqual('name:like:' + nameFilterValue);
      });

      it('updates items when name state filter is updated', function() {
        var stateFilterValue = 'test',
            spyArgs;

        context.createController(0);

        this.controller.stateFilterUpdated(stateFilterValue);
        this.scope.$digest();

        spyArgs = context.getEntitiesLastCallArgs();
        expect(spyArgs).toBeArray();
        expect(spyArgs[0]).toBeObject();
        expect(spyArgs[0].filter).toEqual('state::' + stateFilterValue);
      });

      it('updates items when name page number is changed', function() {
        var page = 2,
            spyArgs;

        context.createController(0);

        this.controller.pagerOptions.page = page;
        this.controller.pageChanged();
        this.scope.$digest();

        spyArgs = context.getEntitiesLastCallArgs()[0];
        expect(spyArgs).toBeObject();
        expect(spyArgs.page).toEqual(page);
      });

      it('has valid display state when there are no entities for criteria', function() {
        context.createController(0);

        this.controller.stateFilterUpdated('test');
        this.scope.$digest();

        expect(this.controller.displayState).toBe(1); // NO_RESULTS
      });

      it('has valid display state when there are entities for criteria', function() {
        context.createController(1);

        this.controller.stateFilterUpdated('test');
        this.scope.$digest();

        expect(this.controller.displayState).toBe(2); // NO_RESULTS
      });

      xit('has valid display state when there are no entities', function() {
        context.createController(0);
        expect(this.controller.displayState).toBe(0); // NO_ENTITIES
      });

    });
  }

  return sharedBehaviour;
});
