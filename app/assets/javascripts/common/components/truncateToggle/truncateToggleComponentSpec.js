/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { DirectiveTestSuiteMixin } from 'test/mixins/DirectiveTestSuiteMixin';
import ngModule from '../../../app';

describe('Component: truncateToggle', function() {
  var textEmptyWarning = 'text not entered yet.';

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, DirectiveTestSuiteMixin);
      this.injectDependencies('$rootScope', '$compile', '$filter', 'gettextCatalog');

      this.createController = (text, toggleLength) => {
        this.createControllerInternal(
          `<truncate-toggle
             text="vm.text"
             toggle-length="vm.toggleLength"
             text-empty-warning="${textEmptyWarning}">
          </truncate-toggle>`,
          {
            text:         text,
            toggleLength: toggleLength
          });
      };
    });
  });

  it('pressing the button truncates the string', function() {
    var divs,
        buttons,
        text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
        '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

    this.createController(text, 20);

    divs = angular.element(this.element[0].getElementsByClassName('col-md-12'));
    expect(divs.length).toBe(1);
    expect(divs.eq(0).text()).toBe(text);

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
    buttons.eq(0).click();
    expect(divs.eq(0).text().length).toBe(this.controller.toggleLength);
  });

  it('pressing the button twice displays whole string', function() {
    var divs,
        buttons,
        text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
        '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

    this.createController(text, 20);

    divs = angular.element(this.element[0].getElementsByClassName('col-md-12'));
    expect(divs.length).toBe(1);

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
    buttons.eq(0).click();
    buttons.eq(0).click();
    expect(divs.eq(0).text()).toBe(text);
  });

  it('button is labelled correctly', function() {
    var buttons,
        text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
        '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

    this.createController(text, 20);
    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
    expect(buttons.eq(0).text().trim()).toBe(this.gettextCatalog.getString('Show less'));
  });

  it('if text is null then warning message is displayed', function() {
    var divs,
        text = '';

    this.createController(text, 20);
    divs = angular.element(this.element[0].getElementsByClassName('alert'));
    expect(divs.length).toBe(1);
    expect(divs.eq(0).text().trim()).toBe(textEmptyWarning);
  });

});
