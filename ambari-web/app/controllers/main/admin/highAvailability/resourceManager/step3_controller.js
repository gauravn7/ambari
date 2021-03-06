/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @typedef {object} rmHaConfigDependencies
 * @property {string|number} webAddressPort
 * @property {string|number} httpsWebAddressPort
 * @property {string|number} zkClientPort
 */

var App = require('app');
require('utils/configs/rm_ha_config_initializer');

App.RMHighAvailabilityWizardStep3Controller = Em.Controller.extend({
  name: "rMHighAvailabilityWizardStep3Controller",

  selectedService: null,

  versionLoaded: true,

  hideDependenciesInfoBar: true,

  isLoaded: false,

  isSubmitDisabled: Em.computed.not('isLoaded'),

  loadStep: function () {
    this.renderConfigs();
  },

  /**
   * Render configs to show them in <code>App.ServiceConfigView</code>
   */
  renderConfigs: function () {

    var configs = $.extend(true, {}, require('data/HDP2/rm_ha_properties').haConfig);

    var serviceConfig = App.ServiceConfig.create({
      serviceName: configs.serviceName,
      displayName: configs.displayName,
      configCategories: [],
      showConfig: true,
      configs: []
    });

    configs.configCategories.forEach(function (configCategory) {
      if (App.Service.find().someProperty('serviceName', configCategory.name)) {
        serviceConfig.configCategories.pushObject(configCategory);
      }
    }, this);

    this.renderConfigProperties(configs, serviceConfig);
    App.ajax.send({
      name: 'config.tags',
      sender: this,
      success: 'loadConfigTagsSuccessCallback',
      error: 'loadConfigsErrorCallback',
      data: {
        serviceConfig: serviceConfig
      }
    });

  },

  loadConfigTagsSuccessCallback: function (data, opt, params) {
    var urlParams = '(type=zoo.cfg&tag=' + data.Clusters.desired_configs['zoo.cfg'].tag + ')|' +
      '(type=yarn-site&tag=' + data.Clusters.desired_configs['yarn-site'].tag + ')|' +
      '(type=yarn-env&tag=' + data.Clusters.desired_configs['yarn-env'].tag + ')';
    App.ajax.send({
      name: 'reassign.load_configs',
      sender: this,
      data: {
        urlParams: urlParams,
        serviceConfig: params.serviceConfig
      },
      success: 'loadConfigsSuccessCallback',
      error: 'loadConfigsSuccessCallback'
    });
  },

  loadConfigsSuccessCallback: function (data, opt, params) {
    params = params.serviceConfig ? params.serviceConfig : arguments[4].serviceConfig;

    this.setDynamicConfigValues(params, data);
    this.setProperties({
      selectedService: params,
      isLoaded: true
    });
  },

  /**
   * Get dependencies for new configs
   *
   * @param {{items: object[]}} data
   * @returns {rmHaConfigDependencies}
   * @private
   * @method _prepareDependencies
   */
  _prepareDependencies: function (data) {
    var ret = {};
    var zooCfg = data && data.items ? data.items.findProperty('type', 'zoo.cfg') : null;
    var yarnSite = data && data.items ? data.items.findProperty('type', 'yarn-site') : null;
    var portValue = zooCfg && Em.get(zooCfg, 'properties.clientPort');
    var webAddressPort = yarnSite && yarnSite.properties ? yarnSite.properties['yarn.resourcemanager.webapp.address'] : '';
    var httpsWebAddressPort = yarnSite && yarnSite.properties ? yarnSite. properties['yarn.resourcemanager.webapp.https.address'] : '';

    ret.webAddressPort = webAddressPort && webAddressPort.contains(':') ? webAddressPort.split(':')[1] : '8088';
    ret.httpsWebAddressPort = httpsWebAddressPort && httpsWebAddressPort.contains(':') ? httpsWebAddressPort.split(':')[1] : '8090';
    ret.zkClientPort = portValue ? portValue : '2181';
    return ret;
  },

  /**
   * Set values to the new configs
   *
   * @param {object} configs
   * @param {object} data
   * @returns {object}
   * @method setDynamicConfigValues
   */
  setDynamicConfigValues: function (configs, data) {
    var topologyLocalDB = this.get('content').getProperties(['masterComponentHosts', 'slaveComponentHosts', 'hosts']);
    var yarnUser = data.items.findProperty('type', 'yarn-env').properties.yarn_user;
    App.RmHaConfigInitializer.setup({
      yarnUser: yarnUser
    });
    var dependencies = this._prepareDependencies(data);
    /** add dynamic property 'hadoop.proxyuser.' + yarnUser + '.hosts' **/
    var proxyUserConfig = App.ServiceConfigProperty.create(App.config.createDefaultConfig('hadoop.proxyuser.' + yarnUser + '.hosts',
      'core-site', false,  {category : "HDFS", isUserProperty: false, isEditable: false, isOverridable: false, serviceName: 'MISC'}));
    configs.configs.pushObject(proxyUserConfig);

    configs.configs.forEach(function (config) {
      App.RmHaConfigInitializer.initialValue(config, topologyLocalDB, dependencies);
    });
    App.RmHaConfigInitializer.cleanup();
    return configs;
  },

  /**
   * Load child components to service config object
   * @param _componentConfig
   * @param componentConfig
   */
  renderConfigProperties: function (_componentConfig, componentConfig) {
    _componentConfig.configs.forEach(function (_serviceConfigProperty) {
      var serviceConfigProperty = App.ServiceConfigProperty.create(_serviceConfigProperty);
      componentConfig.configs.pushObject(serviceConfigProperty);
      serviceConfigProperty.set('isEditable', serviceConfigProperty.get('isReconfigurable'));
      serviceConfigProperty.validate();
    }, this);
  },

  submit: function () {
    if (!this.get('isSubmitDisabled')) {
      App.get('router.mainAdminKerberosController').getKDCSessionState(function() {
        App.router.send("next");
      });
    }
  }
});

