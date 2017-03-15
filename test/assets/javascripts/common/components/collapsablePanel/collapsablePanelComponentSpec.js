/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('collapsablePanelComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/collapsablePanel/collapsablePanel.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');

      this.createScope = function (heading) {
        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<collapsable-panel heading="' + heading + '"><collapsable-panel>',
          undefined,
          'collapsablePanel');
      };
    }));

    it('has valid scope', function() {
      var heading = this.factory.stringNext();
      this.createScope(heading);
      expect(this.controller.heading).toEqual(heading);
      expect(this.controller.panelOpen).toBeTrue();
    });

    it('panel can be collapsed', function() {
      var heading = this.factory.stringNext(),
          panelState;
      this.createScope(heading);
      panelState = this.controller.panelOpen;
      this.controller.panelButtonClicked();
      expect(this.controller.panelOpen).toBe(!panelState);
    });


  });

});
