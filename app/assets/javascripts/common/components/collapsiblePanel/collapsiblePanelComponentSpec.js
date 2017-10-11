/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('collapsiblePanelComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');

      this.createController = (heading) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<collapsible-panel heading="' + heading + '"><collapsible-panel>',
          undefined,
          'collapsiblePanel');
    });
  });

  it('has valid scope', function() {
    var heading = this.factory.stringNext();
    this.createController(heading);
    expect(this.controller.heading).toEqual(heading);
    expect(this.controller.panelOpen).toBeTrue();
  });

  it('panel can be collapsed', function() {
    var heading = this.factory.stringNext(),
        panelState;
    this.createController(heading);
    panelState = this.controller.panelOpen;
    this.controller.panelButtonClicked();
    expect(this.controller.panelOpen).toBe(!panelState);
  });

});
