/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('selectCentreComponent', function() {

  var panelHeader = 'selectCentre directive header';

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');
      this.createController = (options) => {
        this.createControllerInternal(
          `<select-centre panel-header="{{vm.panelHeader}}"
                          get-centres="vm.getCentres"
                          on-centre-selected="vm.onCentreSelected"
                          limit="vm.limit"
                          message-no-results="No results match the criteria."
                          icon="glyphicon-ok-circle">
           </select-centre>`,
          Object.assign({ panelHeader:  panelHeader }, options),
          'selectCentre');
      };

      this.createGetCentresFn = (centres) =>
        (pagerOptions) =>
          this.$q.when({
            items:    centres.slice(0, pagerOptions.limit),
            page:     0,
            offset:   0,
            total:    centres.length,
            limit:    pagerOptions.limit,
            maxPages: centres.length / pagerOptions.limit
          });
    });
  });

  it('displays the list of centres', function() {
    var centres = _.range(20).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });

    expect(this.element.find('li.list-group-item').length).toBe(limit);
    expect(this.element.find('input').length).toBe(1);
  });

  it('displays the pannel header correctly', function() {
    var centres = _.range(20).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    expect(this.element.find('h3').text()).toBe(panelHeader);
  });

  it('has a name filter', function() {
    var centres = [ this.Factory.centre() ];
    this.createController({ getCentres: this.createGetCentresFn(centres), limit: centres.length });
    expect(this.element.find('input').length).toBe(1);
  });


  it('displays pagination controls', function() {
    var centres = _.range(20).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    expect(this.controller.showPagination).toBe(true);
    expect(this.element.find('ul.pagination-sm').length).toBe(1);
  });

  it('updates to name filter cause centres to be re-loaded', function() {
    var centres = _.range(20).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    spyOn(this.controller, 'getCentres').and.callThrough();

    [
      { callCount: 1, nameFilter: 'test' },
      { callCount: 2, nameFilter: '' }
    ].forEach((obj) => {
      this.controller.nameFilter = obj.nameFilter;
      this.controller.nameFilterUpdated();
      expect(this.controller.getCentres.calls.count()).toBe(obj.callCount);
    });
  });

  it('page change causes centres to be re-loaded', function() {
    var centres = _.range(8).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    spyOn(this.controller, 'getCentres').and.callThrough();
    this.controller.pageChanged();
    expect(this.controller.getCentres).toHaveBeenCalled();
  });

  it('clear filter causes centres to be re-loaded', function() {
    var centres = _.range(8).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    spyOn(this.controller, 'getCentres').and.callThrough();
    this.controller.clearFilter();
    expect(this.controller.getCentres).toHaveBeenCalled();
  });

  it('studyGlyphicon returns valid image tag', function() {
    var centres = _.range(8).map(() => this.Factory.centre()),
        limit = centres.length / 2;

    this.createController({ getCentres: this.createGetCentresFn(centres), limit: limit });
    expect(this.controller.centreGlyphicon())
      .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
  });

});
